package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.unique.ArmamentsAction;
import com.megacrit.cardcrawl.actions.unique.ExpertiseAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.green.Tactician;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.tools.GridCardManipulator;
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.SimpleGridCardSelectBuilder;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class TacticianEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Strategist;
            case 2:
                return TacticalLayout;
            case 3:
                return PlanB;
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
                    Field strings = Tactician.class.getDeclaredField("cardStrings");
                    strings.setAccessible(true);
                    CardStrings cardStrings = (CardStrings) strings.get(card);
                    upgradeDescription(card, cardStrings.UPGRADE_DESCRIPTION);
                } catch (Exception e) {
                    Nature.Log(card.name + " failed to fetch original description");
                }
            });
            add(() -> {
                upgradeEvolvedTexts(card, Strategist);
                setBaseCost(card, 0);
                setMagic(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, TacticalLayout);
                setBaseCost(card, 0);
                setMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, PlanB);
                setMagic(card, 1);
                setBlock(card, 10);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Tactician.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(TacticianEvo.class, ctMethodToPatch))
                Nature.Log("Tactician is not evolvable.");
        }
    }

    @SpirePatch(clz = Tactician.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Tactician _inst) {
            if (_inst instanceof EvolvableCard && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Tactician.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Tactician _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Strategist:
                        addToBot(new SimpleHandCardSelectBuilder(AbstractCard::canUpgrade)
                                .setForUpgrade(true)
                                .setAmount(1)
                                .setAnyNumber(false)
                                .setCanPickZero(false)
                                .setShouldMatchAll(true)
                                .setMsg(ArmamentsAction.TEXT[0])
                                .setManipulator(new HandCardManipulator() {
                                    @Override
                                    public boolean manipulate(AbstractCard card, int index) {
                                        card.upgrade();
                                        card.superFlash();
                                        card.applyPowers();
                                        return true;
                                    }
                                })
                        );
                        return SpireReturn.Return(null);
                    case TacticalLayout:
                        for (int i = 0; i < _inst.magicNumber; i++) {
                            addToBot(new SimpleGridCardSelectBuilder(AbstractCard::canUpgrade)
                                    .setForUpgrade(true)
                                    .setAmount(1)
                                    .setAnyNumber(false)
                                    .setCanCancel(false)
                                    .setShouldMatchAll(true)
                                    .setMsg(ArmamentsAction.TEXT[0])
                                    .setDisplayInOrder(false)
                                    .setCardGroup(p.drawPile)
                                    .setManipulator(new GridCardManipulator() {
                                        @Override
                                        public boolean manipulate(AbstractCard card, int index, CardGroup cardGroup) {
                                            card.upgrade();
                                            AbstractDungeon.effectsQueue.add(new UpgradeShineEffect(Settings.WIDTH / 2F, 
                                                    Settings.HEIGHT / 2F));
                                            AbstractDungeon.effectsQueue.add(new ShowCardBrieflyEffect(card.makeStatEquivalentCopy()));
                                            if (p.drawPile.contains(card)) {
                                                p.drawPile.removeCard(card);
                                                p.drawPile.addToTop(card);
                                            }
                                            return false;
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
    
    @SpirePatch(clz = Tactician.class, method = "canUse")
    public static class CanUse {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass ctCanUseType = pool.get(boolean.class.getName());
            CtClass p = pool.get(AbstractPlayer.class.getName());
            CtClass m = pool.get(AbstractMonster.class.getName());
            CtMethod originCanUse = ctClass.getDeclaredMethod("canUse");
            CtMethod canUse = CtNewMethod.make(ctCanUseType, "canUse", new CtClass[]{p, m}, null, 
                    "{ if (" + TacticianEvo.class.getName() + ".CanUseModifier($0, $1, $2)" + ") {" +
                            "return super.canUse($1, $2); } this.cantUseMessage = cardStrings.EXTENDED_DESCRIPTION[0];" +
                            "return false; }", ctClass);
            ctClass.removeMethod(originCanUse);
            ctClass.addMethod(canUse);
        }
    }
    
    public static boolean CanUseModifier(Tactician card, AbstractPlayer p, AbstractMonster m) {
        if (card instanceof EvolvableCard && card.upgraded) {
            return ((EvolvableCard) card).evolanch() == Strategist || ((EvolvableCard) card).evolanch() == TacticalLayout;
        }
        return false;
    }

    @SpirePatch(clz = Tactician.class, method = "triggerOnManualDiscard")
    public static class TriggerOnManualDiscard {
        @SpirePostfixPatch
        public static void Postfix(Tactician _inst) {
            addToTop(new GainBlockAction(LMSK.Player(), LMSK.Player(), _inst.block));
        }
    }
}