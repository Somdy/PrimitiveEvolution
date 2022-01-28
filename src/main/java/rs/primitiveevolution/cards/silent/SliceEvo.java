package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ModifyDamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Slice;
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
import rs.lazymankits.actions.tools.HandCardManipulator;
import rs.lazymankits.actions.utility.DamageCallbackBuilder;
import rs.lazymankits.actions.utility.SimpleHandCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.cards.ironclad.RampageEvo;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.BleedingPower;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;
import static rs.primitiveevolution.datas.BranchID.*;

public class SliceEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Lacerate;
            case 2:
                return Incise;
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
                upgradeEvolvedTexts(card, Lacerate);
                setDamage(card, 6);
                setMagic(card, 6);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Incise);
                setDamage(card, 4);
                setBaseCost(card, 1);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Slice.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(SliceEvo.class, ctMethodToPatch))
                Nature.Log("Slice is not evolvable.");
        }
    }

    @SpirePatch(clz = Slice.class, method = "upgrade")
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

    @SpirePatch(clz = Slice.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Lacerate:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), SLASH_DIAGONAL));
                        addToBot(new ApplyPowerAction(m, p, new BleedingPower(m, _inst.magicNumber)));
                        return SpireReturn.Return(null);
                    case Incise:
                        addToBot(new DamageCallbackBuilder(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage, 
                                _inst.damageTypeForTurn), SLASH_DIAGONAL, c -> {
                            if (c.lastDamageTaken > 0) {
                                addToTop(new SimpleHandCardSelectBuilder(card -> card != _inst)
                                        .setAmount(1)
                                        .setAnyNumber(false)
                                        .setCanPickZero(false)
                                        .setMsg(getIncreasePropertiesUI(c.lastDamageTaken))
                                        .setManipulator(new HandCardManipulator() {
                                            @Override
                                            public boolean manipulate(AbstractCard card, int index) {
                                                if (card.baseDamage > 0)
                                                    card.baseDamage += c.lastDamageTaken;
                                                if (card.baseBlock > 0)
                                                    card.baseBlock += c.lastDamageTaken;
                                                if (card.baseMagicNumber > 0)
                                                    card.magicNumber = card.baseMagicNumber += c.lastDamageTaken;
                                                if (card.baseDiscard > 0)
                                                    card.baseDiscard += c.lastDamageTaken;
                                                if (card.baseDraw > 0)
                                                    card.baseDraw += c.lastDamageTaken;
                                                if (card.baseHeal > 0)
                                                    card.baseHeal += c.lastDamageTaken;
                                                return true;
                                            }
                                        })
                                );
                            }
                        }));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}