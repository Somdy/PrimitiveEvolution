package rs.primitiveevolution.cards.ironclad;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Carnage;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.StarBounceEffect;
import com.megacrit.cardcrawl.vfx.combat.ViolentAttackEffect;
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
import java.util.Optional;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.BLUNT_HEAVY;
import static rs.primitiveevolution.datas.BranchID.*;

public class CarnageEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Slaughter;
            case 2:
                return Injury;
            case 3:
                return WildBlow;
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
                upgradeDamage(card, 8);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Slaughter);
                setDamage(card, 10);
                setMagic(card, 4);
                setBaseCost(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Injury);
                setDamage(card, 22);
            });
            add(() -> {
                upgradeEvolvedTexts(card, WildBlow);
                setDamage(card, 0);
                setBaseCost(card, 2);
                setMagic(card, 3);
                card.target = AbstractCard.CardTarget.ALL_ENEMY;
                card.isEthereal = false;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Carnage.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(CarnageEvo.class, ctMethodToPatch))
                Nature.Log("Carnage is not evolvable.");
        }
    }

    @SpirePatch(clz = Carnage.class, method = "upgrade")
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

    @SpirePatch(clz = Carnage.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Slaughter:
                        if (Settings.FAST_MODE) {
                            addToBot(new VFXAction(new ViolentAttackEffect(m.hb.cX, m.hb.cY, Color.RED)));
                            for (int i = 0; i < 5; i++)
                                addToBot(new VFXAction(new StarBounceEffect(m.hb.cX, m.hb.cY)));
                        } else {
                            addToBot(new VFXAction(new ViolentAttackEffect(m.hb.cX, m.hb.cY, Color.RED), 0.4F));
                            for (int i = 0; i < 5; i++)
                                addToBot(new VFXAction(new StarBounceEffect(m.hb.cX, m.hb.cY)));
                        }
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), BLUNT_HEAVY));
                        return SpireReturn.Return(null);
                    case Injury:
                        if (Settings.FAST_MODE) {
                            addToBot(new VFXAction(new ViolentAttackEffect(m.hb.cX, m.hb.cY, Color.RED)));
                            for (int i = 0; i < 5; i++)
                                addToBot(new VFXAction(new StarBounceEffect(m.hb.cX, m.hb.cY)));
                        } else {
                            addToBot(new VFXAction(new ViolentAttackEffect(m.hb.cX, m.hb.cY, Color.RED), 0.4F));
                            for (int i = 0; i < 5; i++)
                                addToBot(new VFXAction(new StarBounceEffect(m.hb.cX, m.hb.cY)));
                        }
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), BLUNT_HEAVY));
                        if (m.powers.stream().anyMatch(po -> po.type == AbstractPower.PowerType.DEBUFF))
                            addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                    _inst.damageTypeForTurn), BLUNT_HEAVY));
                        return SpireReturn.Return(null);
                    case WildBlow:
                        addToBot(new QuickAction(() -> {
                            List<AbstractCard> cards = LMSK.GetAllExptCards(c -> c.type == AbstractCard.CardType.ATTACK
                                            && c.damage > 0, LMSK.GetALLUnexhaustedCards());
                            int count = Math.min(_inst.magicNumber, cards.size());
                            int totalDamage = 0;
                            while (count >= 0) {
                                Optional<AbstractCard> opt = LMSK.GetRandom(cards, LMSK.CardRandomRng());
                                if (opt.isPresent()) {
                                    count--;
                                    totalDamage += opt.get().damage;
                                    Nature.Log("Obtaining " + opt.get().name + "'s damage: " + opt.get().damage);
                                }
                            }
                            if (totalDamage > 0) {
                                addToTop(new NullableSrcDamageAction(AbstractDungeon.getRandomMonster(),
                                        new CustomDmgInfo(new DamageSource(p, _inst), totalDamage, _inst.damageTypeForTurn), BLUNT_HEAVY));
                                addToTop(new VFXAction(new ViolentAttackEffect(m.hb.cX, m.hb.cY, Color.RED)));
                                for (int i = 0; i < 5; i++)
                                    addToTop(new VFXAction(new StarBounceEffect(m.hb.cX, m.hb.cY)));
                            }
                        }));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Carnage.class, method = SpirePatch.CONSTRUCTOR)
    public static class ApplyPowerMethods {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass voidType = pool.get(void.class.getName());
            CtClass m = pool.get(AbstractMonster.class.getName());
            CtMethod applyPowers = CtNewMethod.make(voidType, "applyPowers", new CtClass[0], null,
                    "{int realBase = this.baseDamage; if(" + CarnageEvo.class.getName() + ".isSlaughter(this)) " +
                            "this.baseDamage += this.magicNumber * " + CarnageEvo.class.getName() + ".slaughters(this);" +
                            "super.applyPowers(); this.baseDamage = realBase; this.isDamageModified = this.baseDamage != this.damage;}", ctClass);
            CtMethod calcd = CtNewMethod.make(voidType, "calculateCardDamage", new CtClass[]{m}, null,
                    "{int realBase = this.baseDamage; if(" + CarnageEvo.class.getName() + ".isSlaughter(this)) " +
                            "this.baseDamage += this.magicNumber * " + CarnageEvo.class.getName() + ".slaughters(this);" +
                            "super.calculateCardDamage($$); this.baseDamage = realBase; " +
                            "this.isDamageModified = this.baseDamage != this.damage;}", ctClass);
            ctClass.addMethod(applyPowers);
            ctClass.addMethod(calcd);
        }
    }
    
    public static boolean isSlaughter(AbstractCard card) {
        return card instanceof EvolvableCard && ((EvolvableCard) card).evolanch() == Slaughter;
    }
    
    public static int slaughters(AbstractCard card) {
        int amount = 0;
        for (AbstractCard c : LMSK.Player().hand.group) {
            if (c.type == AbstractCard.CardType.ATTACK && c != card)
                amount++;
        }
        return amount;
    }
}