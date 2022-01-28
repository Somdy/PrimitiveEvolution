package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Rage;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.FuryPower;
import rs.primitiveevolution.powers.IrritabilityPower;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class RageEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Irritability;
            case 2:
                return Fury;
            case 3:
                return Frenzy;
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
                upgradeMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Irritability);
                card.exhaust = true;
            });
            add(() -> {
                upgradeEvolvedTexts(card, Fury);
                setMagic(card, 4);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Frenzy);
                setMagic(card, 4);
                card.target = AbstractCard.CardTarget.NONE;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Rage.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(RageEvo.class, ctMethodToPatch))
                Nature.Log("Rage is not evolvable.");
        }
    }

    @SpirePatch(clz = Rage.class, method = "upgrade")
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

    @SpirePatch(clz = Rage.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Irritability:
                        addToBot(new ApplyPowerAction(p, p, new IrritabilityPower(1)));
                        return SpireReturn.Return(null);
                    case Fury:
                        addToBot(new ApplyPowerAction(p, p, new FuryPower(_inst.magicNumber)));
                        return SpireReturn.Return(null);
                    case Frenzy:
                        addToBot(new DrawExptCardAction(p, _inst.magicNumber, c -> c.type == AbstractCard.CardType.ATTACK,
                                new AbstractGameAction() {
                                    @Override
                                    public void update() {
                                        isDone = true;
                                        for (AbstractCard card : DrawCardAction.drawnCards) {
                                            int cost = LMSK.CardRandomRng().random(0, 3);
                                            card.setCostForTurn(cost);
                                        }
                                    }
                                }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}