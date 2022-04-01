package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.PlayTopCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Overclock;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.DrawReductionPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.Overdrives;
import static rs.primitiveevolution.datas.BranchID.Overloads;

public class OverclockEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Overloads;
            case 2:
                return Overdrives;
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
                upgradeMagic(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Overloads);
                setMagic(card, 4);
                card.cardsToPreview = null;
            });
            add(() -> {
                upgradeEvolvedTexts(card, Overdrives);
                setMagic(card, 2);
                card.exhaust = true;
                card.target = AbstractCard.CardTarget.ALL_ENEMY;
                card.cardsToPreview = new Dazed();
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = Overclock.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(OverclockEvo.class, ctMethodToPatch))
                Nature.Log("Overclock is not evolvable.");
        }
    }
    
    @SpirePatch(clz = Overclock.class, method = "upgrade")
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
    
    @SpirePatch(clz = Overclock.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Overloads:
                        addToBot(new DrawExptCardAction(_inst.magicNumber, c -> c.costForTurn > 0 && !c.freeToPlay()));
                        addToBot(new ApplyPowerAction(p, p, new DrawReductionPower(p, 1)));
                        return SpireReturn.Return();
                    case Overdrives:
                        addToBot(new QuickAction(() -> {
                            for (int i = 0; i < _inst.magicNumber; i++) {
                                addToTop(new MakeTempCardInDiscardAction(new Dazed(), 1));
                                addToTop(new PlayTopCardAction(AbstractDungeon.getRandomMonster(), false));
                            }
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}