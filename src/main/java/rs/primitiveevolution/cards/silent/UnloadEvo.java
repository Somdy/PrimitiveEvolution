package rs.primitiveevolution.cards.silent;

import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DiscardSpecificCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Unload;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;
import static rs.primitiveevolution.datas.BranchID.Unload_1;
import static rs.primitiveevolution.datas.BranchID.Unload_2;

public class UnloadEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Unload_1;
            case 2:
                return Unload_2;
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
                upgradeEvolvedTexts(card, Unload_1);
                setDamage(card, 16);
                setMagic(card, 4);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Unload_2);
                setDamage(card, 14);
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = Unload.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(UnloadEvo.class, ctMethodToPatch))
                Nature.Log("Unload is not evolvable.");
        }
    }
    
    @SpirePatch(clz = Unload.class, method = "upgrade")
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
    
    @SpirePatch(clz = Unload.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Unload_1:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), SLASH_DIAGONAL));
                        addToBot(new QuickAction(() -> {
                            for (AbstractCard card : p.hand.group) {
                                if (card.type == AbstractCard.CardType.ATTACK && (card.costForTurn > 0 || !card.freeToPlayOnce)) {
                                    addToTop(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                            _inst.magicNumber, _inst.damageTypeForTurn), 
                                            LMSK.GetRandom(LMSK.ListFromObjs(SLASH_HORIZONTAL, SLASH_VERTICAL)).get()));
                                    addToTop(new DiscardSpecificCardAction(card));
                                }
                            }
                        }));
                        return SpireReturn.Return(null);
                    case Unload_2:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), getEffect(_inst.damage)));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
        
        private static AbstractGameAction.AttackEffect getEffect(int damage) {
            if (damage < 15)
                return SLASH_DIAGONAL;
            if (damage < 26)
                return SLASH_VERTICAL;
            if (damage < 37)
                return SLASH_HEAVY;
            else 
                return BLUNT_HEAVY;
        }
    }
    
    @SpirePatch(clz = Unload.class, method = SpirePatch.CONSTRUCTOR)
    public static class ApplyPowerMethods {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtClass m = pool.get(AbstractMonster.class.getName());
            CtMethod calcd = CtNewMethod.make(voidType, "calculateCardDamage", new CtClass[]{m}, null,
                    "{int real = this.baseDamage; if(" + UnloadEvo.class.getName()
                            + ".isLethal($0)) this.baseDamage += " + UnloadEvo.class.getName() + ".LethalDamage($0, $$); " +
                            "super.calculateCardDamage($$); this.baseDamage = real;" +
                            " this.isDamageModified = this.damage != this.baseDamage;}", ctClass);
            ctClass.addMethod(calcd);
        }
    }
    
    public static boolean isLethal(AbstractCard _inst) {
        return _inst instanceof EvolvableCard && ((EvolvableCard) _inst).evolanch() == Unload_2;
    }
    
    public static int LethalDamage(AbstractCard card, AbstractMonster m) {
        int loss = m.maxHealth - m.currentHealth;
        float lossPercent = loss / (m.maxHealth + m.currentHealth * 0.2F);
        return MathUtils.ceil(card.baseDamage * lossPercent);
    }
}