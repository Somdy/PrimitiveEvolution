package rs.primitiveevolution.cards.ironclad;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.WildStrike;
import com.megacrit.cardcrawl.cards.status.Wound;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.utility.DamageCallbackBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.managers.LMExptMgr;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.SLASH_HEAVY;
import static rs.primitiveevolution.datas.BranchID.FreeStrike;
import static rs.primitiveevolution.datas.BranchID.IntrepidStrike;

public class WildStrikeEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return IntrepidStrike;
            case 2:
                return FreeStrike;
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
                upgradeDamage(card, 5);
            });
            add(() -> {
                upgradeEvolvedTexts(card, IntrepidStrike);
                setDamage(card, 24);
                setMagic(card, 2);
                card.cardsToPreview = null;
            });
            add(() -> {
                upgradeEvolvedTexts(card, FreeStrike);
                setDamage(card, 18);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = WildStrike.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(WildStrikeEvo.class, ctMethodToPatch))
                Nature.Log("Wild Strike is not evolvable.");
        }
    }

    @SpirePatch(clz = WildStrike.class, method = "upgrade")
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

    @SpirePatch(clz = WildStrike.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case IntrepidStrike:
                        addToBot(new DamageCallbackBuilder(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage, 
                                _inst.damageTypeForTurn), SLASH_HEAVY, c -> {
                            if (!LMExptMgr.FATAL_JUGDE.test(c)) {
                                addToTop(new ApplyPowerAction(p, c, new VulnerablePower(p, _inst.magicNumber, !c.isPlayer)));
                            }
                        }));
                        return SpireReturn.Return(null);
                    case FreeStrike:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage, 
                                _inst.damageTypeForTurn), SLASH_HEAVY));
                        if (WildStrikeField.unfree.get(_inst)) {
                            addToBot(new MakeTempCardInDiscardAction(new Wound(), 2));
                        }
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch(clz = WildStrike.class, method = SpirePatch.CLASS)
    public static class WildStrikeField {
        public static SpireField<Boolean> unfree = new SpireField<>(() -> false);
    }
    
    @SpirePatch(clz = WildStrike.class, method = SpirePatch.CONSTRUCTOR)
    public static class HasEnoughEnergy {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass boolType = pool.get(boolean.class.getName());
            CtMethod hasEnoughEnergy = CtNewMethod.make(boolType, "hasEnoughEnergy", new CtClass[0], null, 
                    "{ if (" + WildStrikeEvo.class.getName() + ".applyFreeStrike($0)) return true;" +
                            " return super.hasEnoughEnergy(); }", ctClass);
            ctClass.addMethod(hasEnoughEnergy);
        }
    }
    
    public static boolean applyFreeStrike(WildStrike _inst) {
        if (_inst instanceof EvolvableCard && _inst.upgraded && ((EvolvableCard) _inst).evolanch() == FreeStrike) {
            if (EnergyPanel.totalCount >= _inst.costForTurn || _inst.freeToPlay() || _inst.isInAutoplay) {
                WildStrikeField.unfree.set(_inst, false);
                _inst.glowColor = new Color(0.2F, 0.9F, 1.0F, 0.25F);
            } else {
                WildStrikeField.unfree.set(_inst, true);
                _inst.glowColor = Color.RED.cpy();
            }
            return true;
        }
        return false;
    }
}