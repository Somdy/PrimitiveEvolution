package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Cleave;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.CleaveEffect;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.BetterDamageAllEnemiesAction;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.BruisePower;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.BreakThem;
import static rs.primitiveevolution.datas.BranchID.LethalCleave;

public class CleaveEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return LethalCleave;
            case 2:
                return BreakThem;
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
                upgradeEvolvedTexts(card, LethalCleave);
                setDamage(card, 10);
                setMagic(card, 6);
            });
            add(() -> {
                upgradeEvolvedTexts(card, BreakThem);
                setDamage(card, 12);
                setMagic(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Cleave.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(CleaveEvo.class, ctMethodToPatch))
                Nature.Log("Cleave is not evolvable.");
        }
    }

    @SpirePatch(clz = Cleave.class, method = "upgrade")
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

    @SpirePatch(clz = Cleave.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case LethalCleave:
                        addToBot(new SFXAction("ATTACK_HEAVY"));
                        addToBot(new VFXAction(p, new CleaveEffect(), 0.1F));
                        addToBot(new BetterDamageAllEnemiesAction(new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.NONE, true, 
                                c -> {
                                    if (c.lastDamageTaken > 0)
                                        addToTop(new ApplyPowerAction(c, p, new BruisePower(c, _inst.magicNumber)));
                                }));
                        return SpireReturn.Return(null);
                    case BreakThem:
                        addToBot(new SFXAction("ATTACK_HEAVY"));
                        addToBot(new VFXAction(p, new CleaveEffect(), 0.1F));
                        addToBot(new BetterDamageAllEnemiesAction(new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.NONE, true));
                        addToBot(new DrawExptCardAction(p, _inst.magicNumber, c -> c.type == AbstractCard.CardType.ATTACK && c.damage < _inst.damage));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}