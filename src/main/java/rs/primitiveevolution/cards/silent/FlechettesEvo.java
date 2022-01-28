package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.unique.FlechetteAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.green.Flechettes;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.vfx.combat.ThrowDaggerEffect;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.CardAboveCreatureAction;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.utility.DamageCallbackBuilder;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.patches.branchupgrades.BranchableUpgradePatch;
import rs.lazymankits.patches.branchupgrades.HandCardSelectFixPatch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class FlechettesEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return ToxicDarts;
            case 2:
                return ThrowingDarts;
            case 3:
                return ConcealedDarts;
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
                upgradeDamage(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, ToxicDarts);
                setDamage(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, ThrowingDarts);
                setDamage(card, 4);
                setMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, ConcealedDarts);
                setDamage(card, 8);
                setBaseCost(card, -2);
                card.target = AbstractCard.CardTarget.ALL_ENEMY;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Flechettes.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(FlechettesEvo.class, ctMethodToPatch))
                Nature.Log("Flechettes is not evolvable.");
        }
    }

    @SpirePatch(clz = Flechettes.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Flechettes _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Flechettes.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Flechettes _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case ToxicDarts:
                        final int damage = _inst.damage;
                        addToBot(new AbstractGameAction() {
                            @Override
                            public void update() {
                                isDone = true;
                                for (AbstractCard card : p.hand.group) {
                                    if (card.type == AbstractCard.CardType.SKILL) {
                                        addToTop(new DamageCallbackBuilder(m, new CustomDmgInfo(new DamageSource(p, _inst), 
                                                damage, _inst.damageTypeForTurn), AttackEffect.NONE, c -> {
                                            if (c.lastDamageTaken > 0)
                                                addToTop(new ApplyPowerAction(c, p, new PoisonPower(c, p, c.lastDamageTaken)));
                                        }));
                                        if (m != null && m.hb != null)
                                            addToTop(new VFXAction(new ThrowDaggerEffect(m.hb.cX, m.hb.cY)));
                                    }
                                }
                            }
                        });
                        _inst.rawDescription = ((EvolvableCard) _inst).getEvolvedText(ToxicDarts);
                        _inst.initializeDescription();
                        return SpireReturn.Return(null);
                    case ThrowingDarts:
                        addToBot(new FlechetteAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn)));
                        addToBot(new QuickAction(() -> {
                            for (AbstractCard card : p.hand.group) {
                                if (card.type == AbstractCard.CardType.SKILL) {
                                    card.flash();
                                    upgradeBlock(card, _inst.magicNumber);
                                }
                            }
                        }));
                        _inst.rawDescription = ((EvolvableCard) _inst).getEvolvedText(ThrowingDarts);
                        _inst.initializeDescription();
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Flechettes.class, method = "applyPowers")
    public static class ApplyPowers {
        @SpireInsertPatch(rloc = 2)
        public static SpireReturn Insert(Flechettes _inst) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                if (((EvolvableCard) _inst).evolanch() == ConcealedDarts)
                    return SpireReturn.Return(null);
            }
            if (BranchableUpgradePatch.OptFields.SelectingBranch.get(AbstractDungeon.gridSelectScreen)
                    || HandCardSelectFixPatch.HandOptFields.SelectingBranch.get(AbstractDungeon.handCardSelectScreen)) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
        @SpireInsertPatch(rloc = 10)
        public static SpireReturn Insert2(Flechettes _inst) {
            if (_inst instanceof EvolvableCard && _inst.upgraded
                    && !BranchableUpgradePatch.OptFields.SelectingBranch.get(AbstractDungeon.gridSelectScreen)
                    && !HandCardSelectFixPatch.HandOptFields.SelectingBranch.get(AbstractDungeon.handCardSelectScreen)) {
                if (((EvolvableCard) _inst).evolanch() != ConcealedDarts && ((EvolvableCard) _inst).evolanch() != 0) {
                    _inst.rawDescription = ((EvolvableCard) _inst).getEvolvedText(((EvolvableCard) _inst).evolanch());
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Flechettes.class, method = "onMoveToDiscard")
    public static class OnMoveToDiscard {
        @SpirePostfixPatch
        public static void Postfix(Flechettes _inst) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                _inst.rawDescription = ((EvolvableCard) _inst).getEvolvedText(((EvolvableCard) _inst).evolanch());
                _inst.initializeDescription();
            }
        }
    }

    @SpirePatch(clz = Flechettes.class, method = SpirePatch.CONSTRUCTOR)
    public static class CanUse {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass ctCanUseType = pool.get(boolean.class.getName());
            CtClass p = pool.get(AbstractPlayer.class.getName());
            CtClass m = pool.get(AbstractMonster.class.getName());
            CtMethod canUse = CtNewMethod.make(ctCanUseType, "canUse", new CtClass[]{p, m}, null,
                    "{ if (" + FlechettesEvo.class.getName() + ".CanUseModifier($0, $1, $2)" + ") {" +
                            "return false; } return super.canUse($1, $2); }", ctClass);
            ctClass.addMethod(canUse);
        }
    }

    public static boolean CanUseModifier(Flechettes card, AbstractPlayer p, AbstractMonster m) {
        if (card instanceof EvolvableCard && card.upgraded) {
            return ((EvolvableCard) card).evolanch() == ConcealedDarts;
        }
        return false;
    }
    
    @SpirePatch(clz = Flechettes.class, method = SpirePatch.CONSTRUCTOR)
    public static class TriggerOnOtherCardPlayed {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtClass c = pool.get(AbstractCard.class.getName());
            CtMethod toocp = CtNewMethod.make(voidType, "triggerOnOtherCardPlayed", new CtClass[]{c}, null,
                    "return " + FlechettesEvo.class.getName() + ".TOOCPModifier($0, $1);", ctClass);
            ctClass.addMethod(toocp);
        }
    }
    
    public static void TOOCPModifier(Flechettes _inst, AbstractCard c) {
        if (LMSK.Player().hand.contains(_inst) && _inst instanceof EvolvableCard && _inst.upgraded
                && ((EvolvableCard) _inst).evolanch() == ConcealedDarts) {
            if (c.type == AbstractCard.CardType.SKILL) {
                AbstractMonster m = AbstractDungeon.getRandomMonster();
                addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(LMSK.Player(), _inst), 
                        _inst.damage, _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.NONE));
                addToBot(new VFXAction(new ThrowDaggerEffect(m.hb.cX, m.hb.cY)));
                addToBot(new CardAboveCreatureAction(m, _inst));
            }
        }
    }
}