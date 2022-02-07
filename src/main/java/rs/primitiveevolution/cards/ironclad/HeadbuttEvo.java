package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.red.Headbutt;
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
import rs.lazymankits.actions.tools.GridCardManipulator;
import rs.lazymankits.actions.utility.DamageCallbackBuilder;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleGridCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
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

public class HeadbuttEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return IronHead;
            case 2:
                return HeadToHead;
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
                upgradeEvolvedTexts(card, IronHead);
                setDamage(card, 11);
            });
            add(() -> {
                upgradeEvolvedTexts(card, HeadToHead);
                setDamage(card, 11);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Headbutt.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(HeadbuttEvo.class, ctMethodToPatch))
                Nature.Log("Headbutt is not evolvable.");
        }
    }

    @SpirePatch(clz = Headbutt.class, method = "upgrade")
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

    @SpirePatch(clz = Headbutt.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case IronHead:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), BLUNT_HEAVY));
                        addToBot(new QuickAction(() -> {
                            if (p.discardPile.group.stream().anyMatch(c -> c.type == AbstractCard.CardType.SKILL)) {
                                Optional<AbstractCard> opt = LMSK.GetExptCard(c -> c.type == AbstractCard.CardType.SKILL, p.discardPile.group);
                                opt.ifPresent(c -> p.discardPile.moveToHand(c));
                            }
                        }));
                        return SpireReturn.Return();
                    case HeadToHead:
                        addToBot(new DamageCallbackBuilder(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), BLUNT_HEAVY, mo -> {
                            if (mo instanceof AbstractMonster && ((AbstractMonster) mo).getIntentBaseDmg() >= 0) {
                                addToTop(new SimpleGridCardSelectBuilder(c -> c.type == AbstractCard.CardType.SKILL)
                                        .setDisplayInOrder(false)
                                        .setAmount(1)
                                        .setAnyNumber(false)
                                        .setCanCancel(false)
                                        .setShouldMatchAll(true)
                                        .setMsg(getGridToHandSelectUI(1, false))
                                        .setCardGroup(p.drawPile)
                                        .setManipulator(new GridCardManipulator() {
                                            @Override
                                            public boolean manipulate(AbstractCard card, int index, CardGroup cg) {
                                                p.drawPile.moveToHand(card);
                                                return false;
                                            }
                                        })
                                );
                            }
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}