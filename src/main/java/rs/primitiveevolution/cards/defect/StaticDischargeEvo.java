package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.MachineLearning;
import com.megacrit.cardcrawl.cards.blue.StaticDischarge;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.FocusPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.actions.ChannelOverloadedOrbAction;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.ElectrostaticPower;
import rs.primitiveevolution.powers.TrancePower;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class StaticDischargeEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return StaticCharge;
            case 2:
                return Discharge;
            case 3:
                return Electrostatic;
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
                try {
                    Field cardStrings = StaticDischarge.class.getDeclaredField("cardStrings");
                    cardStrings.setAccessible(true);
                    CardStrings strings = (CardStrings) cardStrings.get(card);
                    upgradeDescription(card, strings.UPGRADE_DESCRIPTION);
                } catch (Exception e) {
                    Nature.Log(card.name + " failed to fetch original description");
                }
            });
            add(() -> {
                upgradeEvolvedTexts(card, StaticCharge);
                setBaseCost(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Discharge);
                card.isInnate = true;
            });
            add(() -> {
                upgradeEvolvedTexts(card, Electrostatic);
                setBaseCost(card, 2);
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = StaticDischarge.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(StaticDischargeEvo.class, ctMethodToPatch))
                Nature.Log("Static Discharge is not evolvable.");
        }
    }
    
    @SpirePatch(clz = StaticDischarge.class, method = "upgrade")
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
    
    @SpirePatch(clz = StaticDischarge.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case StaticCharge:
                        addToBot(new QuickAction(() -> {
                            if (!p.masterDeck.isEmpty()) {
                                int count = 0;
                                for (AbstractCard card : p.masterDeck.group) {
                                    if (card.type == AbstractCard.CardType.POWER)
                                        count++;
                                }
                                if (count > 0) {
                                    addToBot(new ApplyPowerAction(p, p, new FocusPower(p, count)));
                                    addToBot(new ApplyPowerAction(p, p, new TrancePower(p, count, count)));
                                }
                            }
                        }));
                        return SpireReturn.Return();
                    case Discharge:
                        addToBot(new QuickAction(() -> {
                            if (!p.hand.isEmpty()) {
                                for (AbstractCard card : p.hand.group) {
                                    if (card.type == AbstractCard.CardType.SKILL)
                                        addToBot(new ChannelOverloadedOrbAction(ChannelOverloadedOrbAction.OrbType.Lightning));
                                }
                            }
                        }));
                        return SpireReturn.Return();
                    case Electrostatic:
                        addToBot(new ApplyPowerAction(p, p, new ElectrostaticPower(p, _inst.magicNumber)));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}