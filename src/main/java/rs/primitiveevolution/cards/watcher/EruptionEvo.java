package rs.primitiveevolution.cards.watcher;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.purple.Eruption;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.ExitStanceEndTurnPower;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.FIRE;
import static rs.primitiveevolution.datas.BranchID.Glare;
import static rs.primitiveevolution.datas.BranchID.Irate;

public class EruptionEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Glare;
            case 2:
                return Irate;
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
                upgradeBaseCost(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Glare);
                setDamage(card, 12);
                setBaseCost(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Irate);
                setDamage(card, 12);
                setBaseCost(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Eruption.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(EruptionEvo.class, ctMethodToPatch))
                Nature.Log("Eruption is not evolvable.");
        }
    }

    @SpirePatch(clz = Eruption.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst) {
            if (_inst instanceof EvolvableCard && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Eruption.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Glare:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), FIRE));
                        addToBot(new ChangeStanceAction("Wrath"));
                        addToBot(new ApplyPowerAction(p, p, new ExitStanceEndTurnPower()));
                        return SpireReturn.Return(null);
                    case Irate:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), FIRE));
                        addToBot(new ChangeStanceAction("Wrath"));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Eruption.class, method = SpirePatch.CONSTRUCTOR)
    public static class ApplyPowerMethods {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtClass m = pool.get(AbstractMonster.class.getName());
            CtMethod applyPowers = CtNewMethod.make(voidType, "applyPowers", new CtClass[0], null, 
                    "{int real = this.baseDamage; if(" + AbstractDungeon.class.getName()
                            + ".player.stance.ID.equals(\"Wrath\")) " +
                            "this.baseDamage *= 2; super.applyPowers(); this.baseDamage = real;" +
                            " this.isDamageModified = this.damage != this.baseDamage;}", ctClass);
            CtMethod calcd = CtNewMethod.make(voidType, "calculateCardDamage", new CtClass[]{m}, null,
                    "{int real = this.baseDamage; if(" + AbstractDungeon.class.getName()
                            + ".player.stance.ID.equals(\"Wrath\")) " +
                            "this.baseDamage *= 2; super.calculateCardDamage($$); this.baseDamage = real;" +
                            " this.isDamageModified = this.damage != this.baseDamage;}", ctClass);
            ctClass.addMethod(applyPowers);
            ctClass.addMethod(calcd);
        }
    }
}