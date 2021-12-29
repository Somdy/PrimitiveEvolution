package rs.primitiveevolution.cards.colorless;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.colorless.Apotheosis;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.Eulogist;

public class ApotheosisEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Eulogist;
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
                upgradeEvolvedTexts(card, Eulogist);
                setBaseCost(card, 1);
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = Apotheosis.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(ApotheosisEvo.class, ctMethodToPatch))
                Nature.Log("Apotheosis is not evolvable.");
        }
    }
    
    @SpirePatch(clz = Apotheosis.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst) {
            if (_inst instanceof EvolvableCard && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Apotheosis.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Eulogist:
                        addToBot(new QuickAction(() -> {
                            upgradeAllCardsIn(p.hand);
                            upgradeAllCardsIn(p.drawPile);
                            upgradeAllCardsIn(p.discardPile);
                            upgradeAllCardsIn(p.exhaustPile);
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
        
        private static void upgradeAllCardsIn(CardGroup group) {
            for (AbstractCard card : group.group) {
                if (card.canUpgrade()) {
                    if (card instanceof EvolvableCard) {
                        int max = ((EvolvableCard) card).possibleBranches().size() - 1;
                        int branch = LMSK.CardRandomRng().random(0, max);
                        ((EvolvableCard) card).setChosenBranch(branch);
                    }
                    if (group.type == CardGroup.CardGroupType.HAND)
                        card.superFlash();
                    card.upgrade();
                    card.applyPowers();
                }
            }
        }
    }
}