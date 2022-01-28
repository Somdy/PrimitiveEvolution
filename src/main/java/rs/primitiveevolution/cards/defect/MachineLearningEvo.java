package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.MachineLearning;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.MachineResearchPower;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class MachineLearningEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return MachineResearch;
            case 2:
                return DoctorMachine;
            case 3:
                return MachineInvention;
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
                card.isInnate = true;
                try {
                    Field cardStrings = MachineLearning.class.getDeclaredField("cardStrings");
                    cardStrings.setAccessible(true);
                    CardStrings strings = (CardStrings) cardStrings.get(card);
                    upgradeDescription(card, strings.UPGRADE_DESCRIPTION);
                } catch (Exception e) {
                    Nature.Log(card.name + " failed to fetch original description");
                }
            });
            add(() -> upgradeEvolvedTexts(card, MachineResearch));
            add(() -> {
                upgradeEvolvedTexts(card, DoctorMachine);
                setMagic(card, 3);
                card.target = AbstractCard.CardTarget.NONE;
                card.isInnate = true;
            });
            add(() -> {
                upgradeEvolvedTexts(card, MachineInvention);
                setMagic(card, 2);
                card.target = AbstractCard.CardTarget.NONE;
                card.isInnate = true;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = MachineLearning.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(MachineLearningEvo.class, ctMethodToPatch))
                Nature.Log("Machine Learning is not evolvable.");
        }
    }

    @SpirePatch(clz = MachineLearning.class, method = "upgrade")
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

    @SpirePatch(clz = MachineLearning.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case MachineResearch:
                        addToBot(new ApplyPowerAction(p, p, new MachineResearchPower(_inst.magicNumber)));
                        return SpireReturn.Return();
                    case DoctorMachine:
                        addToBot(new DrawExptCardAction(p, _inst.magicNumber, c -> c.type == AbstractCard.CardType.SKILL,
                                new AbstractGameAction() {
                                    @Override
                                    public void update() {
                                        isDone = true;
                                        for (AbstractCard card : DrawCardAction.drawnCards) {
                                            card.setCostForTurn(card.costForTurn - 1);
                                        }
                                    }
                                }));
                        return SpireReturn.Return();
                    case MachineInvention:
                        addToBot(new DrawExptCardAction(p, _inst.magicNumber, c -> c.type == AbstractCard.CardType.POWER,
                                new AbstractGameAction() {
                                    @Override
                                    public void update() {
                                        isDone = true;
                                        for (AbstractCard card : DrawCardAction.drawnCards) {
                                            card.setCostForTurn(card.costForTurn - 1);
                                            card.isEthereal = true;
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