package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Concentrate;
import com.megacrit.cardcrawl.cards.green.Skewer;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.DrawReductionPower;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.DamageCallbackBuilder;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.lazymankits.actions.utility.SimpleXCostActionBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.listeners.UseCardListener;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;
import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;

public class SkewerEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return ImpaleBlock;
            case 2:
                return AttackAndDefend;
            case 3:
                return ComboStrikes;
            default:
                return 0;
        }
    }

    @NotNull
    @Contract("_ -> new")
    public static List<UpgradeBranch> branches(AbstractCard card) {
        return new ArrayList<UpgradeBranch>() {{
            add(() -> {
                upgradeName(card);
                upgradeDamage(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, ImpaleBlock);
                setDamage(card, 8);
                setMagic(card, 5);
            });
            add(() -> {
                upgradeEvolvedTexts(card, AttackAndDefend);
                setDamage(card, 8);
            });
            add(() -> {
                upgradeEvolvedTexts(card, ComboStrikes);
                setDamage(card, 7);
                setBlock(card, 4);
                setBaseCost(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Skewer.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(SkewerEvo.class, ctMethodToPatch))
                Nature.Log("Skewer is not evolvable.");
        }
    }

    @SpirePatch(clz = Skewer.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Skewer _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Skewer.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Skewer _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case ImpaleBlock:
                        addToBot(new SimpleXCostActionBuilder(_inst.freeToPlayOnce, _inst.energyOnUse, _inst.upgraded)
                                .addEffect((index, origin) -> origin)
                                .addAction(effect -> {
                                    for (int i = 0; i < effect; i++) {
                                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                                _inst.damage, _inst.damageTypeForTurn), BLUNT_LIGHT));
                                    }
                                })
                                .build()
                        );
                        return SpireReturn.Return(null);
                    case AttackAndDefend:
                        addToBot(new SimpleXCostActionBuilder(_inst.freeToPlayOnce, _inst.energyOnUse, _inst.upgraded)
                                .addEffect((index, origin) -> origin)
                                .addAction(effect -> {
                                    for (int i = 0; i < effect; i++) {
                                        addToBot(new DamageCallbackBuilder(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                                _inst.damage, _inst.damageTypeForTurn), BLUNT_LIGHT, c -> {
                                            if (c.lastDamageTaken > 0)
                                                addToTop(new GainBlockAction(p, c, c.lastDamageTaken));
                                        }));
                                    }
                                })
                                .build()
                        );
                        return SpireReturn.Return(null);
                    case ComboStrikes:
                        for (int i = 0; i < _inst.block; i++) {
                            addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                    _inst.damage, _inst.damageTypeForTurn), BLUNT_LIGHT));
                        }
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Skewer.class, method = SpirePatch.CONSTRUCTOR)
    public static class CalculateCardDamage {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtClass mo = pool.get(AbstractMonster.class.getName());
            CtMethod calcm = CtNewMethod.make(voidType, "calculateCardDamage", new CtClass[]{mo}, null, 
                    "{ int real = this.baseDamage; if (" + SkewerEvo.class.getName() + ".moreDamage($0, $1)"
                            + ") this.baseDamage += this.magicNumber; super.calculateCardDamage($1); this.baseDamage = real; " +
                            "this.isDamageModified = this.damage != this.baseDamage; }", ctClass);
            ctClass.addMethod(calcm);
        }
    }
    
    public static boolean moreDamage(Skewer card, AbstractMonster mo) {
        if (card instanceof EvolvableCard && card.upgraded && ((EvolvableCard) card).evolanch() == ImpaleBlock) {
            return mo.currentBlock > 0;
        }
        return false;
    }
}