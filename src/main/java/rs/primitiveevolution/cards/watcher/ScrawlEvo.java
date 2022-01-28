package rs.primitiveevolution.cards.watcher;

import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.PlayTopCardAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.purple.Scrawl;
import com.megacrit.cardcrawl.cards.purple.Vigilance;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.actions.utility.DrawUntilMeetsWhatBuilder;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.SketchingPower;
import rs.primitiveevolution.powers.SkippingPower;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class ScrawlEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Scrawling;
            case 2:
                return Skipping;
            case 3:
                return Sketching;
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
                upgradeBaseCost(card, 0);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Scrawling);
                setBaseCost(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Skipping);
                setBaseCost(card, 1);
                card.isInnate = true;
            });
            add(() -> {
                upgradeEvolvedTexts(card, Sketching);
                setBaseCost(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Scrawl.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(ScrawlEvo.class, ctMethodToPatch))
                Nature.Log("Scrawl is not evolvable.");
        }
    }

    @SpirePatch(clz = Scrawl.class, method = "upgrade")
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

    @SpirePatch(clz = Scrawl.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Scrawling:
                        addToBot(new QuickAction(() -> {
                            int drawable = BaseMod.MAX_HAND_SIZE - p.hand.size();
                            if (drawable > 0) {
                                addToTop(new DrawCardAction(drawable, new AbstractGameAction() {
                                    @Override
                                    public void update() {
                                        isDone = true;
                                        for (int i = 0; i < p.drawPile.size(); i++) {
                                            addToTop(new PlayTopCardAction(AbstractDungeon.getRandomMonster(), true));
                                        }
                                    }
                                }));
                            }
                        }));
                        return SpireReturn.Return(null);
                    case Skipping:
                        addToBot(new ApplyPowerAction(p, p, new SkippingPower()));
                        return SpireReturn.Return(null);
                    case Sketching:
                        addToBot(new ApplyPowerAction(p, p, new SketchingPower()));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}