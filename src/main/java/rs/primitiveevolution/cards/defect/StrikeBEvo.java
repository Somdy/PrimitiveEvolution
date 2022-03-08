package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Strike_Blue;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Lightning;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
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
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.BLUNT_LIGHT;
import static rs.primitiveevolution.datas.BranchID.LoadStrikeB;
import static rs.primitiveevolution.datas.BranchID.ReStrikeB;

public class StrikeBEvo extends Evolution {
    
    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return LoadStrikeB;
            case 2:
                return ReStrikeB;
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
                upgradeEvolvedTexts(card, LoadStrikeB);
                setDamage(card, 2);
                setMagic(card, 1);
                card.exhaust = true;
            });
            add(() -> {
                upgradeEvolvedTexts(card, ReStrikeB);
                setDamage(card, 3);
                setMagic(card, 1);
            });
        }};
    }
    
    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }
    
    @SpirePatch(clz = Strike_Blue.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(StrikeBEvo.class, ctMethodToPatch))
                Nature.Log("Strike Blue is not evolvable.");
        }
    }
    
    @SpirePatch(clz = Strike_Blue.class, method = "upgrade")
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
    
    @SpirePatch(clz = Strike_Blue.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case LoadStrikeB:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), BLUNT_LIGHT));
                        addToBot(new QuickAction(() -> {
                            if (!p.orbs.isEmpty() && p.orbs.stream().anyMatch(o -> o instanceof Lightning)) {
                                for (AbstractOrb o : p.orbs) {
                                    if (o instanceof Lightning) {
                                        try {
                                            Field bp = AbstractOrb.class.getDeclaredField("basePassiveAmount");
                                            bp.setAccessible(true);
                                            bp.setInt(o, bp.getInt(o) + _inst.magicNumber);
                                        } catch (Exception ignored) {}
                                        o.applyFocus();
                                    }
                                }
                            }
                        }));
                        return SpireReturn.Return();
                    case ReStrikeB:
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), BLUNT_LIGHT));
                        addToBot(new ApplyPowerAction(p, p, new StrengthPower(p, _inst.magicNumber)));
                        addToBot(new ApplyPowerAction(p, p, new DexterityPower(p, -_inst.magicNumber)));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}