package rs.primitiveevolution.cards.watcher;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.purple.BowlingBash;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.actions.unique.BowlingBash_Decompress_Action;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.cards.Evolution.*;
import static rs.primitiveevolution.datas.BranchID.*;

public class BowlingBashEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return BowlingBash_Critical;
            case 2:
                return BowlingBash_Decompress;
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
                upgradeEvolvedTexts(card, BowlingBash_Critical);
                setDamage(card, 8);
                card.retain = true;
            });
            add(() -> {
                upgradeEvolvedTexts(card, BowlingBash_Decompress);
                setDamage(card, 8);
                card.exhaust = true;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = BowlingBash.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(BowlingBashEvo.class, ctMethodToPatch))
                Nature.Log("Bowling Bash is not evolvable.");
        }
    }

    @SpirePatch(clz = BowlingBash.class, method = "upgrade")
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

    @SpirePatch(clz = BowlingBash.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case BowlingBash_Critical:
                        int count = 0;
                        for (AbstractMonster m2 : AbstractDungeon.getCurrRoom().monsters.monsters) {
                            if (!m2.isDeadOrEscaped())
                            {
                                count++;
                                addToBot(new DamageAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn),
                                        AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                            }
                        }
                        if (count >= 3) {
                            addToBot(new SFXAction("ATTACK_BOWLING"));
                        }
                        return SpireReturn.Return(null);
                    case BowlingBash_Decompress:
                        int cnt = 0;
                        for (AbstractMonster m2 : AbstractDungeon.getCurrRoom().monsters.monsters) {
                            if (!m2.isDeadOrEscaped()) cnt++;
                        }
                        if (cnt >= 3) {
                            addToBot(new SFXAction("ATTACK_BOWLING"));
                        }
                        addToBot(new BowlingBash_Decompress_Action(m, new DamageInfo(p,
                                _inst.damage, _inst.damageTypeForTurn), 1, cnt));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}
