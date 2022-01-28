package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Bash;
import com.megacrit.cardcrawl.cards.red.IronWave;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.vfx.combat.IronWaveEffect;
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

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.SLASH_VERTICAL;
import static rs.primitiveevolution.datas.BranchID.BlastWave;
import static rs.primitiveevolution.datas.BranchID.IronSmite;

public class IronWaveEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return BlastWave;
            case 2:
                return IronSmite;
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
                upgradeDamage(card, 2);
                upgradeBlock(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, BlastWave);
                setDamage(card, 7);
                setBlock(card, 5);
                setMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, IronSmite);
                setDamage(card, 20);
                setBaseCost(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = IronWave.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(IronWaveEvo.class, ctMethodToPatch))
                Nature.Log("Iron Wave is not evolvable.");
        }
    }

    @SpirePatch(clz = IronWave.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(IronWave _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = IronWave.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(IronWave _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case BlastWave:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new WaitAction(0.1F));
                        if (p != null && m != null) {
                            addToBot(new VFXAction(new IronWaveEffect(p.hb.cX, p.hb.cY, m.hb.cX), 0.25F));
                        }
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), SLASH_VERTICAL));
                        addToBot(new ApplyPowerAction(m, p, new WeakPower(m, _inst.magicNumber, false)));
                        return SpireReturn.Return(null);
                    case IronSmite:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), SLASH_VERTICAL));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Bash.class, method = SpirePatch.CONSTRUCTOR)
    public static class CalculateCardDamage {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtClass mo = pool.get(AbstractMonster.class.getName());
            CtMethod calcm = CtNewMethod.make(voidType, "calculateCardDamage", new CtClass[]{mo}, null,
                    "{ int real = this.baseDamage; if (" + IronWaveEvo.class.getName() + ".moreDamage($0, $1)"
                            + ") this.baseDamage += $1.currentBlock; super.calculateCardDamage($1); this.baseDamage = real; " +
                            "this.isDamageModified = this.damage != this.baseDamage; }", ctClass);
            ctClass.addMethod(calcm);
        }
    }

    public static boolean moreDamage(Bash card, AbstractMonster mo) {
        if (card instanceof EvolvableCard && card.upgraded && ((EvolvableCard) card).evolanch() == IronSmite) {
            return mo.currentBlock > 0;
        }
        return false;
    }
}