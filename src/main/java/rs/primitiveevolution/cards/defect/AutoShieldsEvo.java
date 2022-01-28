package rs.primitiveevolution.cards.defect;

import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.cards.purple.Vigilance;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DiscoverAction;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.actions.ChannelOverloadedOrbAction;
import rs.primitiveevolution.actions.ImpulseOrbsAction;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class AutoShieldsEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return IntegratedShields;
            case 2:
                return FrozenShields;
            case 3:
                return OverloadedShields;
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
                upgradeBlock(card, 4);
            });
            add(() -> {
                upgradeEvolvedTexts(card, IntegratedShields);
                setBlock(card, 12);
            });
            add(() -> {
                upgradeEvolvedTexts(card, FrozenShields);
                setBaseCost(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, OverloadedShields);
                setBaseCost(card, 2);
                card.exhaust = true;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = AutoShields.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(AutoShieldsEvo.class, ctMethodToPatch))
                Nature.Log("Auto Shields is not evolvable.");
        }
    }

    @SpirePatch(clz = AutoShields.class, method = "upgrade")
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

    @SpirePatch(clz = AutoShields.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case IntegratedShields:
                        if (p.currentBlock == 0) {
                            addToBot(new GainBlockAction(p, p, _inst.block));
                        }
                        return SpireReturn.Return();
                    case FrozenShields:
                        addToBot(ImpulseOrbsAction.Frost());
                        addToBot(ImpulseOrbsAction.Frost());
                        return SpireReturn.Return();
                    case OverloadedShields:
                        addToBot(new QuickAction(() -> {
                            if (p.currentBlock > 0) {
                                int amt = MathUtils.ceil(p.currentBlock / 2F);
                                addToBot(new ChannelOverloadedOrbAction(ChannelOverloadedOrbAction.OrbType.Frost, 
                                        amt, amt + 3, false));
                                addToBot(new ChannelOverloadedOrbAction(ChannelOverloadedOrbAction.OrbType.Frost,
                                        amt, amt + 3, false));
                            }
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AutoShields.class, method = SpirePatch.CONSTRUCTOR)
    public static class ApplyPowerMethods {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtClass m = pool.get(AbstractMonster.class.getName());
            CtMethod applyPowers = CtNewMethod.make(voidType, "applyPowers", new CtClass[0], null,
                    "{int real = this.baseBlock; " + AbstractPower.class.getName() + " po = " + AbstractDungeon.class.getName()
                            + ".player.getPower(\"Focus\"); if (" + AutoShieldsEvo.class.getName()
                            + ".isIntegrated(this) && po != null) this.baseBlock += po.amount; " +
                            "super.applyPowers(); this.baseBlock = real;" +
                            " this.isBlockModified = this.block != this.baseBlock;}", ctClass);
            CtMethod calcd = CtNewMethod.make(voidType, "calculateCardDamage", new CtClass[]{m}, null,
                    "{int real = this.baseBlock; " + AbstractPower.class.getName() + " po = " + AbstractDungeon.class.getName()
                            + ".player.getPower(\"Focus\"); if (" + AutoShieldsEvo.class.getName()
                            + ".isIntegrated(this) && po != null) this.baseBlock += po.amount; " +
                            "super.calculateCardDamage($$); this.baseBlock = real;" +
                            " this.isBlockModified = this.block != this.baseBlock;}", ctClass);
            ctClass.addMethod(applyPowers);
            ctClass.addMethod(calcd);
        }
    }
    
    public static boolean isIntegrated(AutoShields _inst) {
        return _inst instanceof EvolvableCard && ((EvolvableCard) _inst).evolanch() == IntegratedShields;
    }
}