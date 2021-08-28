package rs.primitiveevolution.cards.watcher;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.purple.Eruption;
import com.megacrit.cardcrawl.cards.purple.Vigilance;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.ExitStanceEndTurnPower;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;
import static rs.primitiveevolution.datas.BranchID.*;

public class VigilanceEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Alertness;
            case 2:
                return Guard;
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
                upgradeEvolvedTexts(card, Alertness);
                setBlock(card, 10);
                setMagic(card, 10);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Guard);
                setBlock(card, 10);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Vigilance.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(VigilanceEvo.class, ctMethodToPatch))
                Nature.Log("Vigilance is not evolvable.");
        }
    }

    @SpirePatch(clz = Vigilance.class, method = "upgrade")
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

    @SpirePatch(clz = Vigilance.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Alertness:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new ChangeStanceAction("Calm"));
                        return SpireReturn.Return(null);
                    case Guard:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new ChangeStanceAction("Calm"));
                        addToBot(new DrawExptCardAction(p, 1, c -> c.type == AbstractCard.CardType.ATTACK,
                                new AbstractGameAction() {
                                    @Override
                                    public void update() {
                                        isDone = true;
                                        for (AbstractCard card : DrawCardAction.drawnCards) {
                                            if (!card.isEthereal)
                                                card.retain = true;
                                        }
                                    }
                                }));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Vigilance.class, method = SpirePatch.CONSTRUCTOR)
    public static class ApplyPowerMethods {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtClass m = pool.get(AbstractMonster.class.getName());
            CtMethod applyPowers = CtNewMethod.make(voidType, "applyPowers", new CtClass[0], null,
                    "{int real = this.baseBlock; if(" + AbstractDungeon.class.getName()
                            + ".player.stance.ID.equals(\"Calm\")) " +
                            "this.baseBlock += this.magicNumber; super.applyPowers(); this.baseBlock = real;" +
                            " this.isBlockModified = this.block != this.baseBlock;}", ctClass);
            CtMethod calcd = CtNewMethod.make(voidType, "calculateCardDamage", new CtClass[]{m}, null,
                    "{int real = this.baseBlock; if(" + AbstractDungeon.class.getName()
                            + ".player.stance.ID.equals(\"Calm\")) " +
                            "this.baseBlock += this.magicNumber; super.calculateCardDamage($$); this.baseBlock = real;" +
                            " this.isBlockModified = this.block != this.baseBlock;}", ctClass);
            ctClass.addMethod(applyPowers);
            ctClass.addMethod(calcd);
        }
    }
}