package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.unique.ExpertiseAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Expertise;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.listeners.TurnEventListener;
import rs.lazymankits.listeners.tools.TurnEvent;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class ExpertiseEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Mastery;
            case 2:
                return Specialization;
            case 3:
                return TroublingThoughts;
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
                upgradeEvolvedTexts(card, Mastery);
                setBaseCost(card, 0);
                setMagic(card, 5);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Specialization);
                setMagic(card, 3);
            });
            add(() -> upgradeEvolvedTexts(card, TroublingThoughts));
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Expertise.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(ExpertiseEvo.class, ctMethodToPatch))
                Nature.Log("Expertise is not evolvable.");
        }
    }

    @SpirePatch(clz = Expertise.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Expertise _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Expertise.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Expertise _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Mastery:
                        addToBot(new ExpertiseAction(p, _inst.magicNumber));
                        return SpireReturn.Return(null);
                    case Specialization:
                        if (!p.hand.isEmpty()) {
                            addToBot(new DrawExptCardAction(p, _inst.magicNumber, c -> c.type == mostType(p)));
                        }
                        return SpireReturn.Return(null);
                    case TroublingThoughts:
                        addToBot(new DrawCardAction(_inst.magicNumber, new AbstractGameAction() {
                            @Override
                            public void update() {
                                isDone = true;
                                for (AbstractCard card : DrawCardAction.drawnCards) {
                                    TurnEventListener.AddNewEndTurnPreDiscardEvent(new TurnEvent(() -> {
                                        if (p.hand.contains(card))
                                            p.hand.moveToBottomOfDeck(card);
                                        if (p.discardPile.contains(card))
                                            p.discardPile.moveToBottomOfDeck(card);
                                        if (p.drawPile.contains(card))
                                            p.drawPile.moveToBottomOfDeck(card);
                                    }).setTimes(1).setRemoveConditions(e -> e.times <= 0));
                                }
                            }
                        }));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
        
        private static AbstractCard.CardType mostType(@NotNull AbstractPlayer p) {
            List<AbstractCard.CardType> tmp = new ArrayList<>();
            for (AbstractCard c : p.hand.group) {
                if (!tmp.contains(c.type)) {
                    tmp.add(c.type);
                }
            }
            int[] types = new int[tmp.size()];
            Arrays.fill(types, 0);
            for (AbstractCard c : p.hand.group) {
                if (tmp.contains(c.type)) {
                    int index = tmp.indexOf(c.type);
                    types[index]++;
                }
            }
            int most = 0;
            int max = types[0];
            for (int i = 0; i < types.length; i++) {
                if (types[i] > max) {
                    most = i;
                    max = types[i];
                }
            }
            if (most < tmp.size())
                return tmp.get(most);
            return LMSK.GetRandom(tmp).orElse(AbstractCard.CardType.SKILL);
        }
    }
}