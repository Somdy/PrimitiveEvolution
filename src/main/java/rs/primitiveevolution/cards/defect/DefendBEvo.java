package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Defend_Blue;
import com.megacrit.cardcrawl.cards.red.Defend_Red;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Frost;
import com.megacrit.cardcrawl.orbs.Lightning;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.powers.PlatedArmorPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.cards.ironclad.DefendREvo;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.TrancePower;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.BLUNT_LIGHT;
import static rs.primitiveevolution.datas.BranchID.*;

public class DefendBEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return FocusDefendB;
            case 2:
                return ColdDefendB;
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
                upgradeBlock(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, FocusDefendB);
                setBlock(card, 7);
                setMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, ColdDefendB);
                setBlock(card, 7);
                setMagic(card, 2);
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = Defend_Blue.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(DefendBEvo.class, ctMethodToPatch))
                Nature.Log("Defend Blue is not evolvable.");
        }
    }
    
    @SpirePatch(clz = Defend_Blue.class, method = "upgrade")
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
    
    @SpirePatch(clz = Defend_Blue.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case FocusDefendB:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new ApplyPowerAction(p, p, new FocusPower(p, _inst.magicNumber)));
                        addToBot(new ApplyPowerAction(p, p, new TrancePower(p, 1, _inst.magicNumber)));
                        return SpireReturn.Return();
                    case ColdDefendB:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new QuickAction(() -> {
                            if (!p.orbs.isEmpty() && p.orbs.stream().anyMatch(o -> o instanceof Frost)) {
                                for (AbstractOrb o : p.orbs) {
                                    if (o instanceof Frost) {
                                        try {
                                            Field bp = AbstractOrb.class.getDeclaredField("baseEvokeAmount");
                                            bp.setAccessible(true);
                                            bp.setInt(o, bp.getInt(o) + _inst.magicNumber);
                                        } catch (Exception ignored) {}
                                        o.applyFocus();
                                    }
                                }
                            }
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}
