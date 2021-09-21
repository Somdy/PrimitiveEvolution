package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Uppercut;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.GainStrengthPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.utility.DamageCallbackBuilder;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.BruisePower;
import rs.primitiveevolution.powers.DamageNextTurnPower;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.BLUNT_HEAVY;
import static rs.primitiveevolution.datas.BranchID.*;

public class UppercutEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return HeavyUppercut;
            case 2:
                return UpperPunch;
            case 3:
                return BrutalUppercut;
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
                upgradeMagic(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, HeavyUppercut);
                setMagic(card, 12);
            });
            add(() -> {
                upgradeEvolvedTexts(card, UpperPunch);
                setDamage(card, 18);
                setMagic(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, BrutalUppercut);
                setBaseCost(card, 1);
                setDamage(card, 10);
                setMagic(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Uppercut.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(UppercutEvo.class, ctMethodToPatch))
                Nature.Log("Uppercut is not evolvable.");
        }
    }

    @SpirePatch(clz = Uppercut.class, method = "upgrade")
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

    @SpirePatch(clz = Uppercut.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case HeavyUppercut:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), 
                                _inst.damage, _inst.damageTypeForTurn), BLUNT_HEAVY));
                        addToBot(new ApplyPowerAction(m, p, new StrengthPower(m, -4)));
                        addToBot(new ApplyPowerAction(m, p, new GainStrengthPower(m, 4)));
                        addToBot(new ApplyPowerAction(m, p, new BruisePower(m, _inst.magicNumber)));
                        return SpireReturn.Return();
                    case UpperPunch:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), BLUNT_HEAVY));
                        addToBot(new ApplyPowerAction(m, p, new VulnerablePower(m, _inst.magicNumber, false)));
                        addToBot(new ApplyPowerAction(m, p, new DamageNextTurnPower(m, p, _inst.damage)));
                        return SpireReturn.Return();
                    case BrutalUppercut:
                        addToBot(new DamageCallbackBuilder(m, new CustomDmgInfo(new DamageSource(p, _inst), 
                                _inst.damage, _inst.damageTypeForTurn), BLUNT_HEAVY, c -> {
                            if (c.lastDamageTaken > 0 && p.powers.stream().anyMatch(po -> po.type == AbstractPower.PowerType.DEBUFF)) {
                                Optional<AbstractPower> opt = p.powers.stream()
                                        .filter(po -> po.type == AbstractPower.PowerType.DEBUFF)
                                        .findAny();
                                opt.ifPresent(po -> {
                                    addToTop(new QuickAction(() -> {
                                        po.owner = c;
                                        po.updateDescription();
                                        addToTop(new ApplyPowerAction(c, p, po));
                                    }));
                                    addToTop(new RemoveSpecificPowerAction(p, p, po));
                                });
                            }
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}