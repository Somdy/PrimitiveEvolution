package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Survivor;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.FrailPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.utility.DelayAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.Survival;
import static rs.primitiveevolution.datas.BranchID.Surviving;

public class SurvivorEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Survival;
            case 2:
                return Surviving;
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
                upgradeEvolvedTexts(card, Survival);
                setBaseCost(card, 0);
                setBlock(card, 8);
            });
            add(() -> upgradeEvolvedTexts(card, Surviving));
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Survivor.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(SurvivorEvo.class, ctMethodToPatch))
                Nature.Log("Survivor is not evolvable.");
        }
    }

    @SpirePatch(clz = Survivor.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Survivor _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Survivor.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Survivor _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Survival:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new DelayAction(() -> {
                            if (EnergyPanel.totalCount <= 0)
                                addToBot(new DiscardAction(p, p, 1, false));
                        }));
                        return SpireReturn.Return(null);
                    case Surviving:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new DelayAction(() -> p.powers.stream()
                                .filter(po -> po instanceof VulnerablePower || po instanceof FrailPower)
                                .forEach(po -> addToTop(new ReducePowerAction(p, p, po, 2)))));
                        addToBot(new DiscardAction(p, p, 1, false));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}