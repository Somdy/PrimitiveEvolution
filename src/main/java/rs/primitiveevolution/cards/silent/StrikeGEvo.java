package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.green.Strike_Green;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.tools.GridCardManipulator;
import rs.lazymankits.actions.utility.DelayAction;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleGridCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;
import static rs.primitiveevolution.datas.BranchID.*;

public class StrikeGEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Strike_G_1;
            case 2:
                return Strike_G_2;
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
                upgradeEvolvedTexts(card, Strike_G_1);
                setDamage(card, 8);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Strike_G_2);
                setDamage(card, 9);
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = Strike_Green.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(StrikeGEvo.class, ctMethodToPatch))
                Nature.Log("Strike Green is not evolvable.");
        }
    }
    
    @SpirePatch(clz = Strike_Green.class, method = "upgrade")
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
    
    @SpirePatch(clz = Strike_Green.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Strike_G_1:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), SLASH_DIAGONAL));
                        return SpireReturn.Return(null);
                    case Strike_G_2:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), SLASH_HORIZONTAL));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Strike_Green.class, method = SpirePatch.CONSTRUCTOR)
    public static class ExtraEffect {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidT = pool.get(void.class.getName());
            CtMethod tomd = CtNewMethod.make(voidT, "triggerOnManualDiscard", new CtClass[0], null,
                    "{if(" + StrikeGEvo.class.getName() + ".CanTriggerOnDiscarded($0))"
                            + StrikeGEvo.class.getName() + ".TriggerDiscardEffect($0); }", ctClass);
            CtMethod orfmd = CtNewMethod.make(voidT, "onRemoveFromMasterDeck", new CtClass[0], null,
                    "{if(" + StrikeGEvo.class.getName() + ".CanTriggerOnRemoval($0))"
                            + StrikeGEvo.class.getName() + ".TriggerRemovalEffect($0); }", ctClass);
            ctClass.addMethod(tomd);
            ctClass.addMethod(orfmd);
        }
    }
    
    public static boolean CanTriggerOnDiscarded(AbstractCard card) {
        if (card instanceof EvolvableCard && card.upgraded) {
            return ((EvolvableCard) card).evolanch() == Strike_G_1;
        }
        return false;
    }
    
    public static void TriggerDiscardEffect(AbstractCard _inst) {
        AbstractMonster m = AbstractDungeon.getRandomMonster();
        AbstractPlayer p = AbstractDungeon.player;
        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                _inst.damage, _inst.damageTypeForTurn), SLASH_HORIZONTAL));
    }
    
    public static boolean CanTriggerOnRemoval(AbstractCard card) {
        if (card instanceof EvolvableCard && card.upgraded) {
            return ((EvolvableCard) card).evolanch() == Strike_G_2;
        }
        return false;
    }
    
    public static void TriggerRemovalEffect(AbstractCard card) {
        Nature.AddToBot(new DelayAction(() -> {
            Nature.AddToBot(new SimpleGridCardSelectBuilder(AbstractCard::canUpgrade)
                    .setForUpgrade(true)
                    .setAmount(1)
                    .setMsg(getGridUpgradeUI(1))
                    .setCardGroup(LMSK.Player().masterDeck)
                    .setCanCancel(false)
                    .setAnyNumber(false)
                    .setManipulator(new GridCardManipulator() {
                        @Override
                        public boolean manipulate(AbstractCard c, int index, CardGroup cardGroup) {
                            c.upgrade();
                            LMSK.Player().bottledCardUpgradeCheck(c);
                            AbstractDungeon.effectsQueue.add(new UpgradeShineEffect(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                            AbstractDungeon.topLevelEffectsQueue.add(new ShowCardBrieflyEffect(c.makeStatEquivalentCopy()));
                            return false;
                        }
                    }));
        }, Settings.ACTION_DUR_XLONG + 1F));
    }
}