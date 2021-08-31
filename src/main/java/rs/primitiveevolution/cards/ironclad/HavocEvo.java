package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Havoc;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rs.primitiveevolution.datas.BranchID.Annihilate;
import static rs.primitiveevolution.datas.BranchID.Catastrophe;

public class HavocEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Annihilate;
            case 2:
                return Catastrophe;
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
                upgradeEvolvedTexts(card, Annihilate);
                setBaseCost(card, 0);
                card.exhaust = true;
            });
            add(() -> {
                upgradeEvolvedTexts(card, Catastrophe);
                setBaseCost(card, -2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Havoc.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(HavocEvo.class, ctMethodToPatch))
                Nature.Log("Havoc is not evolvable.");
        }
    }

    @SpirePatch(clz = Havoc.class, method = "upgrade")
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

    @SpirePatch(clz = Havoc.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Annihilate:
                        addToBot(new SimpleHandCardSelectBuilder(c -> c.costForTurn <= 2)
                                .setAmount(1)
                                .setAnyNumber(false)
                                .setCanPickZero(false)
                                .setMsg(getPlayHandCardUI(true, 1))
                                .setManipulator(new HandCardManipulator() {
                                    @Override
                                    public boolean manipulate(AbstractCard card, int index) {
                                        card.exhaustOnUseOnce = true;
                                        addToTop(new NewQueueCardAction(card, true, true, true));
                                        return false;
                                    }
                                })
                        );
                        return SpireReturn.Return();
                    case Catastrophe:
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Havoc.class, method = SpirePatch.CONSTRUCTOR)
    public static class CatastropheModifiers {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtClass p = pool.get(AbstractPlayer.class.getName());
            CtClass m = pool.get(AbstractMonster.class.getName());
            CtMethod canUse = CtNewMethod.make(voidType, "canUse", new CtClass[]{p, m}, null,
                    "{if (" + HavocEvo.class.getName() + ".CanUseModifier($0, $1, $2)) {" +
                            "return false; } return super.canUse($$);}", ctClass);
            CtMethod twd = CtNewMethod.make(voidType, "triggerWhenDrawn", new CtClass[0], null, 
                    "{if (" + HavocEvo.class.getName() + ".CanUseModifier($0, null, null)) {" +
                            HavocEvo.class.getName() + ".Catastrophe($0, " + AbstractDungeon.class.getName() + ".player);}}", ctClass);
            ctClass.addMethod(canUse);
            ctClass.addMethod(twd);
        }
    }
    
    public static void Catastrophe(Havoc havoc, AbstractPlayer p) {
        addToBot(new ExhaustSpecificCardAction(havoc, p.hand));
        addToBot(new QuickAction(() -> {
            if (!p.drawPile.isEmpty()) {
                Optional<AbstractCard> opt = LMSK.GetRandom(p.drawPile.group, LMSK.CardRandomRng());
                opt.ifPresent(c -> {
                    AbstractCard copy = c.makeStatEquivalentCopy();
                    addToTop(new NewQueueCardAction(copy, true,
                            true, true));
                });
            }
            if (!p.discardPile.isEmpty()) {
                Optional<AbstractCard> opt = LMSK.GetRandom(p.discardPile.group, LMSK.CardRandomRng());
                opt.ifPresent(c -> {
                    AbstractCard copy = c.makeStatEquivalentCopy();
                    addToTop(new NewQueueCardAction(copy, true,
                            true, true));
                });
            }
        }));
    }
    
    public static boolean CanUseModifier(Havoc card, AbstractPlayer p, AbstractMonster m) {
        if (card instanceof EvolvableCard && card.upgraded) {
            return ((EvolvableCard) card).evolanch() == Catastrophe;
        }
        return false;
    }
}