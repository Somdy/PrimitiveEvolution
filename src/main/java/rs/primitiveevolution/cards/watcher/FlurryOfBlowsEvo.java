package rs.primitiveevolution.cards.watcher;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.ModifyDamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.purple.FlurryOfBlows;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.watcher.MarkPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class FlurryOfBlowsEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return FlurryOfBlows_Soft;
            case 2:
                return FlurryOfBlows_Fast;
            case 3:
                return FlurryOfBlows_Power;
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
            });
            add(() -> {
                upgradeEvolvedTexts(card, FlurryOfBlows_Soft);
                setDamage(card, 0);
                setBlock(card, 3);
                setMagic(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, FlurryOfBlows_Fast);
                setDamage(card, 1);
                setMagic(card, 4);
            });
            add(() -> {
                upgradeEvolvedTexts(card, FlurryOfBlows_Power);
                setDamage(card, 2);
                setMagic(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = FlurryOfBlows.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(FlurryOfBlowsEvo.class, ctMethodToPatch))
                Nature.Log("Flurry Of Blows is not evolvable.");
        }
    }

    @SpirePatch(clz = FlurryOfBlows.class, method = "upgrade")
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

    @SpirePatch(clz = FlurryOfBlows.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case FlurryOfBlows_Soft:
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new ApplyPowerAction(m, p, new MarkPower(m, _inst.magicNumber), _inst.magicNumber));
                        return SpireReturn.Return(null);
                    case FlurryOfBlows_Fast:
                        for ( int i = 0; i < _inst.magicNumber; i++ ){
                            addToBot(new DamageAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn),
                                    AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                        }
                        return SpireReturn.Return(null);
                    case FlurryOfBlows_Power:
                        addToBot(new DamageAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn),
                                AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                        addToBot(new ModifyDamageAction(_inst.uuid, _inst.magicNumber));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}
