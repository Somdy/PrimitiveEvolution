package rs.primitiveevolution.cards.watcher;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.actions.watcher.LessonLearnedAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.purple.CrushJoints;
import com.megacrit.cardcrawl.cards.purple.LessonLearned;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.tools.GridCardManipulator;
import rs.lazymankits.actions.utility.DamageCallbackBuilder;
import rs.lazymankits.actions.utility.SimpleGridCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.managers.LMExptMgr;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.actions.unique.CrushJoints_Cripple_Action;
import rs.primitiveevolution.actions.unique.CrushJoints_Vary_Action;
import rs.primitiveevolution.actions.unique.LearnLessonAction;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class LessonLearnedEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return LessonLearned_retain;
            case 2:
                return LessonLearned_hurt;
            case 3:
                return LearnLesson;
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
                upgradeDamage(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, LessonLearned_retain);
                card.retain = true;
            });
            add(() -> {
                upgradeEvolvedTexts(card, LessonLearned_hurt);
                setBaseCost(card, 1);
                setMagic(card, 3);
                card.exhaust = false;
            });
            add(() -> {
                upgradeEvolvedTexts(card, LearnLesson);
                setBaseCost(card, 3);
                setDamage(card, 20);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = LessonLearned.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(LessonLearnedEvo.class, ctMethodToPatch))
                Nature.Log("Lesson Learned is not evolvable.");
        }
    }

    @SpirePatch(clz = LessonLearned.class, method = "upgrade")
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

    @SpirePatch(clz = LessonLearned.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case LessonLearned_retain:
                        addToBot(new LessonLearnedAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn)));
                        return SpireReturn.Return();
                    case LessonLearned_hurt:
                        addToBot(new LoseHPAction(p, p, _inst.magicNumber));
                        addToBot(new LessonLearnedAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn)));
                        return SpireReturn.Return();
                    case LearnLesson:
                        addToBot(new LearnLessonAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), getGridUpgradeUI(1)));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}