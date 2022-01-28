package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Concentrate;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.DrawReductionPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.listeners.UseCardListener;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class ConcentrateEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Focusing;
            case 2:
                return Restless;
            case 3:
                return Indecisive;
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
                upgradeMagic(card, -1);
            });
            add(() -> upgradeEvolvedTexts(card, Focusing));
            add(() -> {
                upgradeEvolvedTexts(card, Restless);
                setMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Indecisive);
                setMagic(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Concentrate.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(ConcentrateEvo.class, ctMethodToPatch))
                Nature.Log("Concentrate is not evolvable.");
        }
    }

    @SpirePatch(clz = Concentrate.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Concentrate _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Concentrate.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Concentrate _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Focusing:
                        addToBot(new GainEnergyAction(2));
                        addToBot(new ApplyPowerAction(p, p, new DrawReductionPower(p, 1)));
                        return SpireReturn.Return(null);
                    case Restless:
                        addToBot(new SimpleHandCardSelectBuilder(c -> !UseCardListener.ContainsUnplayableCard(c))
                                .setAmount(2)
                                .setAnyNumber(false)
                                .setCanPickZero(false)
                                .setMsg(getUnplayableUI(_inst.magicNumber))
                                .setManipulator(new HandCardManipulator() {
                                    @Override
                                    public boolean manipulate(AbstractCard card, int index) {
                                        UseCardListener.AddCustomUnplayableCard(card, _inst.magicNumber, 
                                                (c, plr, mo) -> false, true);
                                        return true;
                                    }
                                })
                        );
                        addToBot(new GainEnergyAction(2));
                        return SpireReturn.Return(null);
                    case Indecisive:
                        addToBot(new DiscardAction(p, p, _inst.magicNumber, false));
                        addToBot(new QuickAction(() -> {
                            for (AbstractCard card : p.hand.group) {
                                card.superFlash();
                                card.setCostForTurn(card.costForTurn - 1);
                            }
                        }));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}