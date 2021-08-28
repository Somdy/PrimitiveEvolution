package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ModifyDamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Rampage;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
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

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.SLASH_DIAGONAL;
import static rs.primitiveevolution.datas.BranchID.*;

public class RampageEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Violence;
            case 2:
                return Rampaging;
            case 3:
                return Thumps;
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
                upgradeMagic(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Violence);
                setMagic(card, 6);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Rampaging);
                setMagic(card, 5);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Thumps);
                setDamage(card, 5);
                setBaseCost(card, 2);
                setMagic(card, 0);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Rampage.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(RampageEvo.class, ctMethodToPatch))
                Nature.Log("Rampage is not evolvable.");
        }
    }

    @SpirePatch(clz = Rampage.class, method = "upgrade")
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

    @SpirePatch(clz = Rampage.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Violence:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), SLASH_DIAGONAL));
                        int incrs = _inst.magicNumber;
                        if (m.currentHealth < (m.maxHealth * 3 / 4))
                            incrs += 6;
                        addToBot(new ModifyDamageAction(_inst.uuid, incrs));
                        return SpireReturn.Return(null);
                    case Rampaging:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), SLASH_DIAGONAL));
                        addToBot(new ModifyDamageAction(_inst.uuid, _inst.magicNumber));
                        addToBot(new DrawExptCardAction(p, 1, c -> c != _inst && c.type == AbstractCard.CardType.ATTACK
                                && c.costForTurn == 1));
                        return SpireReturn.Return(null);
                    case Thumps:
                        for (int i = 0; i < _inst.damage; i++) {
                            addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                    _inst.damageTypeForTurn), SLASH_DIAGONAL));
                        }
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}