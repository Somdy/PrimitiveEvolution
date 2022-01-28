package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Defragment;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.FocusPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.TrancePower;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class DefragmentEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Jigsaw;
            case 2:
                return Cache;
            case 3:
                return Brick;
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
                upgradeEvolvedTexts(card, Jigsaw);
                setMagic(card, 1);
                setBaseCost(card, 0);
                card.isInnate = true;
            });
            add(() -> {
                upgradeEvolvedTexts(card, Cache);
                setMagic(card, 4);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Brick);
                setMagic(card, 3);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Defragment.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(DefragmentEvo.class, ctMethodToPatch))
                Nature.Log("Defragment is not evolvable.");
        }
    }

    @SpirePatch(clz = Defragment.class, method = "upgrade")
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

    @SpirePatch(clz = Defragment.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Jigsaw:
                        addToBot(new ApplyPowerAction(p, p, new FocusPower(p, 1)));
                        addToBot(new DrawExptCardAction(p, 1, c -> c.type == AbstractCard.CardType.POWER));
                        return SpireReturn.Return();
                    case Cache:
                        addToBot(new ApplyPowerAction(p, p, new FocusPower(p, _inst.magicNumber)));
                        addToBot(new ApplyPowerAction(p, p, new TrancePower(p, 2, _inst.magicNumber)));
                        return SpireReturn.Return();
                    case Brick:
                        addToBot(new ApplyPowerAction(p, p, new FocusPower(p, 2)));
                        addToBot(new ApplyPowerAction(p, p, new TrancePower(p, _inst.magicNumber, 2)));
                        addToBot(new DrawExptCardAction(p, 1, c -> c.name.equals(_inst.name) && c != _inst));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}