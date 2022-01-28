package rs.primitiveevolution.cards.watcher;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.actions.watcher.TriggerMarksAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.purple.JustLucky;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.watcher.MantraPower;
import com.megacrit.cardcrawl.powers.watcher.MarkPower;
import com.megacrit.cardcrawl.vfx.combat.FlickCoinEffect;
import com.megacrit.cardcrawl.vfx.combat.PressurePointEffect;
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

public class JustLuckyEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return JustLucky_Holy;
            case 2:
                return JustLucky_PressurePoint;
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
                upgradeBlock(card, 1);
                upgradeDamage(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, JustLucky_Holy);
                setMagic(card, 2);
                setBlock(card, 0);
                upgradeDamage(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, JustLucky_PressurePoint);
                setMagic(card, 2);
                upgradeBlock(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = JustLucky.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(JustLuckyEvo.class, ctMethodToPatch))
                Nature.Log("JustLucky is not evolvable.");
        }
    }

    @SpirePatch(clz = JustLucky.class, method = "upgrade")
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

    @SpirePatch(clz = JustLucky.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case JustLucky_Holy:
                        addToBot(new ScryAction(_inst.magicNumber));
                        addToBot(new VFXAction(new FlickCoinEffect(p.hb.cX, p.hb.cY, m.hb.cX, m.hb.cY), 0.3F));
                        addToBot(new ApplyPowerAction(p, p, new MantraPower(p, _inst.magicNumber), _inst.magicNumber));
                        addToBot(new DamageAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn),
                                AbstractGameAction.AttackEffect.NONE));
                        return SpireReturn.Return(null);
                    case JustLucky_PressurePoint:
                        addToBot(new ScryAction(_inst.magicNumber));
                        addToBot(new VFXAction(new FlickCoinEffect(p.hb.cX, p.hb.cY, m.hb.cX, m.hb.cY), 0.3F));
                        addToBot(new GainBlockAction(p, p, _inst.block));
                        addToBot(new VFXAction(new PressurePointEffect(m.hb.cX, m.hb.cY)));
                        addToBot(new ApplyPowerAction(m, p, new MarkPower(m, _inst.magicNumber), _inst.magicNumber));
                        addToBot(new TriggerMarksAction(_inst));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}
