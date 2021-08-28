package rs.primitiveevolution.cards.ironclad;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.unique.ArmamentsAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Tactician;
import com.megacrit.cardcrawl.cards.red.Armaments;
import com.megacrit.cardcrawl.cards.red.Flex;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.watcher.VigorPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class ArmamentsEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return ArmedToTeeth;
            case 2:
                return ArmAtArms;
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
                try {
                    Field strings = Armaments.class.getDeclaredField("cardStrings");
                    strings.setAccessible(true);
                    CardStrings cardStrings = (CardStrings) strings.get(card);
                    upgradeDescription(card, cardStrings.UPGRADE_DESCRIPTION);
                } catch (Exception e) {
                    Nature.Log(card.name + " failed to fetch original description");
                }
            });
            add(() -> {
                upgradeEvolvedTexts(card, ArmedToTeeth);
                setBlock(card, 4);
            });
            add(() -> {
                upgradeEvolvedTexts(card, ArmAtArms);
                setBaseCost(card, 2);
                card.selfRetain = true;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Armaments.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(ArmamentsEvo.class, ctMethodToPatch))
                Nature.Log("Armaments is not evolvable.");
        }
    }

    @SpirePatch(clz = Armaments.class, method = "upgrade")
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

    @SpirePatch(clz = Armaments.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case ArmedToTeeth:
                        addToBot(new QuickAction(() -> {
                            for (AbstractCard card : p.hand.group) {
                                if (card.upgraded) {
                                    addToTop(new GainBlockAction(p, p, _inst.block));
                                    card.superFlash(Color.GRAY.cpy());
                                }
                                if (card.canUpgrade()) {
                                    card.upgrade();
                                    card.superFlash();
                                    card.applyPowers();
                                }
                            }
                        }));
                        return SpireReturn.Return(null);
                    case ArmAtArms:
                        for (int i = 0; i < 2; i++) {
                            addToBot(new SimpleHandCardSelectBuilder(AbstractCard::canUpgrade)
                                    .setAmount(1)
                                    .setCanPickZero(false)
                                    .setAnyNumber(false)
                                    .setMsg(ArmamentsAction.TEXT[0])
                                    .setManipulator(new HandCardManipulator() {
                                        @Override
                                        public boolean manipulate(AbstractCard card, int index) {
                                            card.upgrade();
                                            card.superFlash();
                                            card.applyPowers();
                                            int max = Math.max(card.block, Math.max(card.magicNumber, card.damage));
                                            if (max > 0)
                                                addToTop(new GainBlockAction(p, p, max));
                                            return true;
                                        }
                                    })
                            );
                        }
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}