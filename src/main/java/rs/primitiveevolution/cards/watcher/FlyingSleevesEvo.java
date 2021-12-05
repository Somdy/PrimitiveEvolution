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
import com.megacrit.cardcrawl.actions.common.RemoveAllBlockAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.watcher.TriggerMarksAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.purple.FlurryOfBlows;
import com.megacrit.cardcrawl.cards.purple.FlyingSleeves;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.watcher.MarkPower;
import com.megacrit.cardcrawl.vfx.combat.AnimatedSlashEffect;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.actions.unique.FlyingSleeves_PressurePoint_Action;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import com.badlogic.gdx.graphics.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;

public class FlyingSleevesEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return FlyingSleeves_Melter;
            case 2:
                return FlyingSleeves_PressurePoint;
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
                upgradeEvolvedTexts(card, FlyingSleeves_Melter);
            });
            add(() -> {
                upgradeEvolvedTexts(card, FlyingSleeves_PressurePoint);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = FlyingSleeves.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(FlyingSleevesEvo.class, ctMethodToPatch))
                Nature.Log("Flying Sleeves is not evolvable.");
        }
    }

    @SpirePatch(clz = FlyingSleeves.class, method = "upgrade")
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

    @SpirePatch(clz = FlyingSleeves.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case FlyingSleeves_Melter:
                        addToBot(new RemoveAllBlockAction(m, p));
                        if (m != null)
                        {
                            addToBot(new SFXAction("ATTACK_WHIFF_2", 0.3F));
                            addToBot(new SFXAction("ATTACK_FAST", 0.2F));
                            addToBot(new VFXAction(new AnimatedSlashEffect(m.hb.cX, m.hb.cY - 30.0F * Settings.scale,
                                    500.0F, 200.0F, 290.0F, 3.0F, Color.VIOLET, Color.PINK)));
                        }
                        addToBot(new DamageAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn),
                                AbstractGameAction.AttackEffect.NONE));
                        if (m != null)
                        {
                            addToBot(new SFXAction("ATTACK_WHIFF_1", 0.2F));
                            addToBot(new SFXAction("ATTACK_FAST", 0.2F));
                            addToBot(new VFXAction(new AnimatedSlashEffect(m.hb.cX, m.hb.cY - 30.0F * Settings.scale,
                                    500.0F, -200.0F, 250.0F, 3.0F, Color.VIOLET, Color.PINK)));
                        }
                        addToBot(new DamageAction(m, new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn),
                                AbstractGameAction.AttackEffect.NONE));
                        return SpireReturn.Return(null);
                    case FlyingSleeves_PressurePoint:
                        addToBot(new FlyingSleeves_PressurePoint_Action(m,
                                new DamageInfo(p, _inst.damage, _inst.damageTypeForTurn)));
                        addToBot(new TriggerMarksAction(_inst));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}
