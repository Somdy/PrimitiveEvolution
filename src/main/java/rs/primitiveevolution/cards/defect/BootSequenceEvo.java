package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.blue.AllForOne;
import com.megacrit.cardcrawl.cards.blue.BiasedCognition;
import com.megacrit.cardcrawl.cards.blue.BootSequence;
import com.megacrit.cardcrawl.cards.blue.Seek;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.actions.tools.GridCardManipulator;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleGridCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class BootSequenceEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return PreBoot;
            case 2:
                return Preload;
            case 3:
                return EmergencySeq;
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
            });
            add(() -> {
                upgradeEvolvedTexts(card, PreBoot);
                setMagic(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Preload);
                setBaseCost(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, EmergencySeq);
                setBaseCost(card, 1);
                card.retain = false;
                card.selfRetain = false;
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = BootSequence.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(BootSequenceEvo.class, ctMethodToPatch))
                Nature.Log("Boot Sequence is not evolvable.");
        }
    }
    
    @SpirePatch(clz = BootSequence.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = BootSequence.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case PreBoot:
                        addToBot(new DrawExptCardAction(_inst.magicNumber, c -> c.type == AbstractCard.CardType.SKILL)
                                .discardPileNotIncluded());
                        return SpireReturn.Return();
                    case Preload:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new QuickAction(() -> {
                            if (!p.drawPile.isEmpty()) {
                                CardGroup tmp = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                                int count = Math.min(5, p.drawPile.size());
                                for (int i = 0; i < count; i++) {
                                    tmp.addToBottom(p.drawPile.group.get(i));
                                }
                                addToTop(new SimpleGridCardSelectBuilder(c -> true)
                                        .setCardGroup(tmp)
                                        .setCanCancel(true)
                                        .setAnyNumber(true)
                                        .setAmount(2)
                                        .setDisplayInOrder(true)
                                        .setShouldMatchAll(true)
                                        .setMsg(getChooseUpToCardsUI(2) + _inst.name)
                                        .setManipulator(new GridCardManipulator() {
                                            @Override
                                            public boolean manipulate(AbstractCard card, int index, CardGroup cardGroup) {
                                                if (p.drawPile.contains(card)) {
                                                    p.drawPile.removeCard(card);
                                                    p.drawPile.addToTop(card);
                                                }
                                                return false;
                                            }
                                        })
                                );
                            }
                        }));
                        return SpireReturn.Return();
                    case EmergencySeq:
                        addToBot(new QuickAction(() -> {
                            CardGroup tmp = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                            tmp.addToTop(new BiasedCognition());
                            tmp.addToTop(new AllForOne());
                            tmp.addToTop(new Seek());
                            addToTop(new SimpleGridCardSelectBuilder(c -> true)
                                    .setCardGroup(tmp)
                                    .setCanCancel(false)
                                    .setAnyNumber(false)
                                    .setAmount(1)
                                    .setDisplayInOrder(true)
                                    .setShouldMatchAll(true)
                                    .setMsg(getChooseUpToCardsUI(1) + _inst.name)
                                    .setManipulator(new GridCardManipulator() {
                                        @Override
                                        public boolean manipulate(AbstractCard card, int index, CardGroup cardGroup) {
                                            addToTop(new NewQueueCardAction(card, true, true, true));
                                            return false;
                                        }
                                    })
                            );
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = BootSequence.class, method = SpirePatch.CONSTRUCTOR)
    public static class ApplyPowerMethods {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtMethod triggerWhenDrawn = CtNewMethod.make(voidType, "triggerWhenDrawn", new CtClass[0], null,
                    "{if(" + BootSequenceEvo.class.getName() + ".isEmergencySeq($0)){$0.selfRetain = true;}}", ctClass);
            ctClass.addMethod(triggerWhenDrawn);
        }
    }
    
    public static boolean isEmergencySeq(AbstractCard card) {
        return card instanceof EvolvableCard && ((EvolvableCard) card).evolanch() == EmergencySeq && card.upgraded;
    }
}