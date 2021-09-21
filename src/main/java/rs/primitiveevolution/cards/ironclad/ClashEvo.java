package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Clash;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.ClashEffect;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.NONE;
import static rs.primitiveevolution.datas.BranchID.HandClash;
import static rs.primitiveevolution.datas.BranchID.Stalemate;

public class ClashEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return HandClash;
            case 2:
                return Stalemate;
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
                upgradeDamage(card, 4);
            });
            add(() -> {
                upgradeEvolvedTexts(card, HandClash);
                setDamage(card, 16);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Stalemate);
                setDamage(card, 20);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Clash.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(ClashEvo.class, ctMethodToPatch))
                Nature.Log("Clash is not evolvable.");
        }
    }

    @SpirePatch(clz = Clash.class, method = "upgrade")
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

    @SpirePatch(clz = Clash.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case HandClash:
                        if (m != null)
                            addToBot(new VFXAction(new ClashEffect(m.hb.cX, m.hb.cY), 0.1F));
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), NONE));
                        return SpireReturn.Return();
                    case Stalemate:
                        if (m != null)
                            addToBot(new VFXAction(new ClashEffect(m.hb.cX, m.hb.cY), 0.1F));
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), NONE));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = Clash.class, method = SpirePatch.CONSTRUCTOR)
    public static class ClashPowersModifier {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass m = pool.get(AbstractMonster.class.getName());
            CtClass bool = pool.get(boolean.class.getName());
            CtClass voidType = pool.get(void.class.getName());
            CtMethod cardPlayable = CtNewMethod.make(bool, "cardPlayable", new CtClass[]{m}, null, 
                    "{boolean canUse = super.cardPlayable($$); if (" + ClashEvo.class.getName() + ".isHandClashEvo($0)) {" +
                            "canUse = ($1 == null || $1.getIntentBaseDmg() > 0);}" +
                            "if (!canUse) this.cantUseMessage = " + ClashEvo.class.getName() + ".clashCantMessage(); return canUse;}", ctClass);
            CtMethod applyPowers = CtNewMethod.make(voidType, "applyPowers", new CtClass[0], null, 
                    "{int real = this.baseDamage; if (" + ClashEvo.class.getName() + ".isStalemateEvo($0)) " +
                            "this.baseDamage -=" + ClashEvo.class.getName() + ".damageModified(); " +
                            "super.applyPowers(); this.baseDamage = real; this.isDamageModified = this.damage != this.baseDamage;}", ctClass);
            CtMethod calccd = CtNewMethod.make(voidType, "calculateCardDamage", new CtClass[]{m}, null,
                    "{int real = this.baseDamage; if (" + ClashEvo.class.getName() + ".isStalemateEvo($0)) " +
                            "this.baseDamage -=" + ClashEvo.class.getName() + ".damageModified(); " +
                            "super.calculateCardDamage($$); this.baseDamage = real; this.isDamageModified = this.damage != this.baseDamage;}", ctClass);
            CtMethod twd = CtNewMethod.make(voidType, "triggerWhenDrawn", new CtClass[0], null, 
                    "{if (" + ClashEvo.class.getName() + ".isStalemateEvo($0) && !this.isEthereal) this.retain = true;}", ctClass);
            ctClass.addMethod(cardPlayable);
            ctClass.addMethod(applyPowers);
            ctClass.addMethod(calccd);
            ctClass.addMethod(twd);
        }
    }
    
    public static int damageModified() {
        int decrement = 0;
        for (AbstractCard card : LMSK.Player().hand.group) {
            if (card.type == AbstractCard.CardType.SKILL)
                decrement += 2;
        }
        return decrement;
    }
    
    public static String clashCantMessage() {
        return getCantUseMessage(Evolution.OnlyOnAttackingOne);
    }

    public static boolean isHandClashEvo(Clash clash) {
        return clash instanceof EvolvableCard && clash.upgraded && ((EvolvableCard) clash).evolanch() == HandClash;
    }
    
    public static boolean isStalemateEvo(Clash clash) {
        return clash instanceof EvolvableCard && clash.upgraded && ((EvolvableCard) clash).evolanch() == Stalemate;
    }
    
    @SpirePatch(clz = Clash.class, method = "canUse")
    public static class CanUseMod {
        @SpireInsertPatch(rloc = 4, localvars = {"canUse"})
        public static SpireReturn<Boolean> Insert(AbstractCard _inst, AbstractPlayer p, AbstractMonster m, boolean canUse) {
            if (_inst instanceof Clash) {
                if (_inst instanceof EvolvableCard && _inst.upgraded && ((EvolvableCard) _inst).evolanch() != 0) {
                    return SpireReturn.Return(canUse);
                }
            }
            return SpireReturn.Continue();
        }
    }
}