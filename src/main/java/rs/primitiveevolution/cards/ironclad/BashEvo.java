package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Bash;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.StrengthPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.BruisePower;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.BLUNT_HEAVY;
import static rs.primitiveevolution.datas.BranchID.Smite;
import static rs.primitiveevolution.datas.BranchID.Whack;

public class BashEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Smite;
            case 2:
                return Whack;
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
                upgradeMagic(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Smite);
                setDamage(card, 8);
                setMagic(card, 6);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Whack);
                setDamage(card, 8);
                setBaseCost(card, 1);
                setMagic(card, 1);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Bash.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(BashEvo.class, ctMethodToPatch))
                Nature.Log("Bash is not evolvable.");
        }
    }

    @SpirePatch(clz = Bash.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Bash _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Bash.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Bash _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Smite:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), BLUNT_HEAVY));
                        addToBot(new ApplyPowerAction(m, p, new BruisePower(m, _inst.magicNumber)));
                        return SpireReturn.Return(null);
                    case Whack:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), BLUNT_HEAVY));
                        addToBot(new ApplyPowerAction(p, p, new StrengthPower(p, _inst.magicNumber)));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}