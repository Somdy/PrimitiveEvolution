package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Prepared;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.PreparationReturnPower;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class PreparedEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return WellPrepared;
            case 2:
                return VisionPreparation;
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
                try {
                    Field cardStrings = Prepared.class.getDeclaredField("cardStrings");
                    cardStrings.setAccessible(true);
                    CardStrings strings = (CardStrings) cardStrings.get(card);
                    upgradeDescription(card, strings.UPGRADE_DESCRIPTION);
                } catch (Exception e) {
                    Nature.Log(card.name + " failed to fetch original description");
                }
            });
            add(() -> {
                upgradeEvolvedTexts(card, WellPrepared);
                setMagic(card, 3);
            });
            add(() -> {
               upgradeEvolvedTexts(card, VisionPreparation);
               setMagic(card, 2);
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = Prepared.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(PreparedEvo.class, ctMethodToPatch))
                Nature.Log("Prepared is not evolvable.");
        }
    }
    
    @SpirePatch(clz = Prepared.class, method = "upgrade")
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
    
    @SpirePatch(clz = Prepared.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case WellPrepared:
                        addToBot(new SimpleHandCardSelectBuilder(c -> true)
                                .setAmount(_inst.magicNumber)
                                .setCanPickZero(true)
                                .setAnyNumber(true)
                                .setMsg(getEvolvedMsg(_inst, WellPrepared, 0))
                                .setManipulator(new HandCardManipulator() {
                                    @Override
                                    public boolean manipulate(AbstractCard card, int index) {
                                        p.hand.moveToDiscardPile(card);
                                        card.triggerOnManualDiscard();
                                        GameActionManager.incrementDiscard(false);
                                        addToBot(new DrawExptCardAction(p, 1, c -> c.type == card.type && c != card));
                                        return false;
                                    }
                                })
                        );
                        return SpireReturn.Return();
                    case VisionPreparation:
                        addToBot(new SimpleHandCardSelectBuilder(c -> true)
                                .setAmount(_inst.magicNumber)
                                .setCanPickZero(false)
                                .setAnyNumber(false)
                                .setMsg(getEvolvedMsg(_inst, VisionPreparation, 0))
                                .setManipulator(new HandCardManipulator() {
                                    @Override
                                    public boolean manipulate(AbstractCard card, int index) {
                                        p.hand.moveToDiscardPile(card);
                                        card.triggerOnManualDiscard();
                                        GameActionManager.incrementDiscard(false);
                                        if (index == 0) {
                                            addToTop(new ApplyPowerAction(p, p, new PreparationReturnPower(card)));
                                        }
                                        return false;
                                    }
                                })
                        );
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}