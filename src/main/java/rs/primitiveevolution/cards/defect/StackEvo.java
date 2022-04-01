package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.Soul;
import com.megacrit.cardcrawl.cards.blue.Stack;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.Reflects;
import static rs.primitiveevolution.datas.BranchID.Stacking;

public class StackEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Stacking;
            case 2:
                return Reflects;
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
                try {
                    Field cardStrings = Stack.class.getDeclaredField("cardStrings");
                    cardStrings.setAccessible(true);
                    CardStrings strings = (CardStrings) cardStrings.get(card);
                    upgradeDescription(card, strings.UPGRADE_DESCRIPTION);
                } catch (Exception e) {
                    Nature.Log(card.name + " failed to fetch original description");
                }
            });
            add(() -> {
                upgradeEvolvedTexts(card, Stacking);
                setBlock(card, 0);
                card.misc = 0;
            });
            add(() -> {
                upgradeEvolvedTexts(card, Reflects);
                setMagic(card, 2);
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = Stack.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(StackEvo.class, ctMethodToPatch))
                Nature.Log("Stack is not evolvable.");
        }
    }
    
    @SpirePatch(clz = Stack.class, method = "upgrade")
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
    
    @SpirePatch(clz = Stack.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Stacking:
                        addToBot(new QuickAction(() -> {
                            int blocks = p.drawPile.size() + _inst.misc;
                            addToBot(new GainBlockAction(p, p, blocks));
                        }));
                        return SpireReturn.Return();
                    case Reflects:
                        addToBot(new DrawExptCardAction(2, c -> c.type != AbstractCard.CardType.POWER, new AbstractGameAction() {
                            @Override
                            public void update() {
                                isDone = true;
                                for (AbstractCard card : DrawCardAction.drawnCards) {
                                    if (card.costForTurn > 0)
                                        addToTop(new GainBlockAction(p, p, card.costForTurn * 2));
                                }
                            }
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Stack.class, method = "applyPowers")
    public static class StackingApplyPowers {
        @SpirePostfixPatch
        public static void Postfix(AbstractCard _inst) {
            if (IsStackingEvo(_inst)) {
                _inst.block = LMSK.Player().drawPile.size();
                _inst.block += _inst.misc;
                _inst.isBlockModified = true;
                _inst.rawDescription = getEvolvedMsg(_inst, Stacking, 0);
                _inst.initializeDescription();
            }
        }
    }
    
    @SpirePatch(clz = Soul.class, method = "shuffle")
    public static class ShuffleStackingPatch {
        @SpirePostfixPatch
        public static void Postfix(Soul _inst, AbstractCard card, boolean i) {
            if (IsStackingEvo(card)) {
                card.misc += 2;
                card.applyPowers();
            }
        }
    }
    
    public static boolean IsStackingEvo(AbstractCard card) {
        return card instanceof EvolvableCard && card instanceof Stack
                && ((EvolvableCard) card).evolanch() == Stacking && card.upgraded;
    }
}