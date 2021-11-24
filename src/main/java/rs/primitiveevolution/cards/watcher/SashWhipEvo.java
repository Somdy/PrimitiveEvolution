package rs.primitiveevolution.cards.watcher;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.purple.SashWhip;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.actions.unique.SashWhip_Cripple_Action;
import rs.primitiveevolution.actions.unique.SashWhip_PressurePoint_Action;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class SashWhipEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return SashWhip_PressurePoint;
            case 2:
                return SashWhip_Cripple;
            default:
                return 0;
        }
    }

    private static final int ATTACK_TIMES = 3;
    @NotNull
    @Contract("_ -> new")
    public static List<UpgradeBranch> branches(AbstractCard card) {
        return new ArrayList<UpgradeBranch>() {{
            add(() -> {
                upgradeName(card);
                upgradeDamage(card, 4);
                upgradeMagic(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, SashWhip_PressurePoint);
                setDamage(card, 3);
                setMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, SashWhip_Cripple);
                setDamage(card, 3);
                setMagic(card, 3);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = SashWhip.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(SashWhipEvo.class, ctMethodToPatch))
                Nature.Log("Sash Whip is not evolvable.");
        }
    }

    @SpirePatch(clz = SashWhip.class, method = "upgrade")
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

    @SpirePatch(clz = SashWhip.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case SashWhip_PressurePoint:
                        for (int i = 0; i < ATTACK_TIMES; i++) {
                            addToBot(new DamageAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn),
                                    AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                        }
                        addToBot(new SashWhip_PressurePoint_Action(m, _inst.magicNumber));
                        return SpireReturn.Return(null);
                    case SashWhip_Cripple:
                        for (int i = 0; i < ATTACK_TIMES; i++) {
                            addToBot(new DamageAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn),
                                    AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                        }
                        addToBot(new SashWhip_Cripple_Action(m, _inst.magicNumber));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}
