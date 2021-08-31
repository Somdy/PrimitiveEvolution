package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Defend_Blue;
import com.megacrit.cardcrawl.cards.blue.HelloWorld;
import com.megacrit.cardcrawl.cards.blue.Strike_Blue;
import com.megacrit.cardcrawl.cards.blue.WhiteNoise;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DiscoverAction;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class WhiteNoiseEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return RandomSignals;
            case 2:
                return LimitedBandwidth;
            case 3:
                return Imply;
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
                upgradeEvolvedTexts(card, RandomSignals);
                setBaseCost(card, 0);
            });
            add(() -> {
                upgradeEvolvedTexts(card, LimitedBandwidth);
                setBaseCost(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Imply);
                setBaseCost(card, 1);
                card.isInnate = true;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = WhiteNoise.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(WhiteNoiseEvo.class, ctMethodToPatch))
                Nature.Log("White Noise is not evolvable.");
        }
    }

    @SpirePatch(clz = WhiteNoise.class, method = "upgrade")
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

    @SpirePatch(clz = WhiteNoise.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case RandomSignals:
                        addToBot(new DiscoverAction(c -> c.type == AbstractCard.CardType.POWER));
                        return SpireReturn.Return();
                    case LimitedBandwidth:
                        addToBot(new QuickAction(() -> {
                            AbstractCard attack = LMSK.ReturnTrulyRndCardInCombat(c -> c.type == AbstractCard.CardType.ATTACK)
                                    .orElse(new Strike_Blue());
                            AbstractCard skill = LMSK.ReturnTrulyRndCardInCombat(c -> c.type == AbstractCard.CardType.SKILL)
                                    .orElse(new Defend_Blue());
                            AbstractCard power = LMSK.ReturnTrulyRndCardInCombat(c -> c.type == AbstractCard.CardType.POWER)
                                    .orElse(new HelloWorld());
                            addToTop(new DiscoverAction(LMSK.ListFromObjs(attack, skill, power), c -> c.setCostForTurn(c.costForTurn - 1)));
                        }));
                        return SpireReturn.Return();
                    case Imply:
                        addToBot(new QuickAction(() -> {
                            AbstractCard power = LMSK.ReturnTrulyRndCardInCombat(c -> c.type == AbstractCard.CardType.POWER)
                                    .orElse(new HelloWorld());
                            power.setCostForTurn(0);
                            addToTop(new MakeTempCardInHandAction(power, 1));
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}