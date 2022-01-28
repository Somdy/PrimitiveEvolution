package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Defend_Green;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.PlatedArmorPower;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.ProtectionPower;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.Defend_G_1;
import static rs.primitiveevolution.datas.BranchID.Defend_G_2;

public class DefendGEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Defend_G_1;
            case 2:
                return Defend_G_2;
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
                upgradeBlock(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Defend_G_1);
                setBlock(card, 8);
                setMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Defend_G_2);
                setBlock(card, 6);
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = Defend_Green.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(DefendGEvo.class, ctMethodToPatch))
                Nature.Log("Defend Green is not evolvable.");
        }
    }
    
    @SpirePatch(clz = Defend_Green.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Defend_Green.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Defend_G_1:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        return SpireReturn.Return(null);
                    case Defend_G_2:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new ApplyPowerAction(p, p, new ProtectionPower(p, 1, true)));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Defend_Green.class, method = SpirePatch.CONSTRUCTOR)
    public static class DiscardEffect {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidT = pool.get(void.class.getName());
            CtMethod tomd = CtNewMethod.make(voidT, "triggerOnManualDiscard", new CtClass[0], null, 
                    "{if(" + DefendGEvo.class.getName() + ".CanTriggerOnDiscarded($0))"
                            + DefendGEvo.class.getName() + ".TriggerDiscardEffect($0); }", ctClass);
            ctClass.addMethod(tomd);
        }
    }
    
    public static boolean CanTriggerOnDiscarded(Defend_Green card) {
        if (card instanceof EvolvableCard && card.upgraded) {
            return ((EvolvableCard) card).evolanch() == Defend_G_1;
        }
        return false;
    }
    
    public static void TriggerDiscardEffect(Defend_Green card) {
        addToBot(new ApplyPowerAction(LMSK.Player(), LMSK.Player(), new PlatedArmorPower(LMSK.Player(), card.magicNumber)));
    }
}