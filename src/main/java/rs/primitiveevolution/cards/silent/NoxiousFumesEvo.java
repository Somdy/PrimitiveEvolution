package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.NoxiousFumes;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.NoxiousFumesPower;
import com.megacrit.cardcrawl.powers.WeakPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.NoxiousDustPower;
import rs.primitiveevolution.powers.NoxiousGasPower;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class NoxiousFumesEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return NoxiousDust;
            case 2:
                return NoxiousGas;
            case 3:
                return NoxiousSmoke;
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
            add(() -> upgradeEvolvedTexts(card, NoxiousDust));
            add(() -> upgradeEvolvedTexts(card, NoxiousGas));
            add(() -> {
                upgradeEvolvedTexts(card, NoxiousSmoke);
                card.target = AbstractCard.CardTarget.SELF_AND_ENEMY;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = NoxiousFumes.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(NoxiousFumesEvo.class, ctMethodToPatch))
                Nature.Log("Noxious Fumes is not evolvable.");
        }
    }

    @SpirePatch(clz = NoxiousFumes.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(NoxiousFumes _inst) {
            if (_inst instanceof EvolvableCard && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = NoxiousFumes.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(NoxiousFumes _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case NoxiousDust:
                        addToBot(new ApplyPowerAction(p, p, new NoxiousDustPower(p, _inst.magicNumber)));
                        return SpireReturn.Return(null);
                    case NoxiousGas:
                        addToBot(new ApplyPowerAction(p, p, new NoxiousGasPower(p, _inst.magicNumber, 2)));
                        return SpireReturn.Return(null);
                    case NoxiousSmoke:
                        for (AbstractMonster mo : LMSK.GetAllExptMstr(c -> true)) {
                            addToBot(new ApplyPowerAction(mo, p, new WeakPower(mo, _inst.magicNumber, false)));
                        }
                        addToBot(new ApplyPowerAction(p, p, new NoxiousFumesPower(p, _inst.magicNumber)));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}