package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Fusion;
import com.megacrit.cardcrawl.cards.blue.SweepingBeam;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.EmptyOrbSlot;
import com.megacrit.cardcrawl.orbs.Plasma;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.powers.LockOnPower;
import com.megacrit.cardcrawl.vfx.combat.SweepingBeamEffect;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.BetterDamageAllEnemiesAction;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.actions.ChannelOverloadedOrbAction;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.TrancePower;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.*;
import static rs.primitiveevolution.actions.ChannelOverloadedOrbAction.OrbType.*;

public class FusionEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Superload;
            case 2:
                return Overfusion;
            case 3:
                return SuperFusion;
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
                upgradeBaseCost(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Superload);
                setBaseCost(card, 2);
                setMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Overfusion);
                setBaseCost(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, SuperFusion);
                setBaseCost(card, 1);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Fusion.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(FusionEvo.class, ctMethodToPatch))
                Nature.Log("Fusion is not evolvable.");
        }
    }

    @SpirePatch(clz = Fusion.class, method = "upgrade")
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

    @SpirePatch(clz = Fusion.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Superload:
                        for (int i = 0; i < _inst.magicNumber; i++) {
                            addToBot(new ChannelOverloadedOrbAction(LMSK.GetRandom(LMSK.ListFromObjs(Lightning, Frost, Dark), 
                                    LMSK.CardRandomRng()).get()));
                        }
                        return SpireReturn.Return();
                    case Overfusion:
                        for (int i = 0; i < _inst.magicNumber; i++) {
                            addToBot(new ChannelAction(new Plasma()));
                        }
                        addToBot(new DrawExptCardAction(p, 2, c -> c.type == AbstractCard.CardType.SKILL));
                        return SpireReturn.Return();
                    case SuperFusion:
                        addToBot(new QuickAction(() -> {
                            List<String> types = new ArrayList<>();
                            for (AbstractOrb o : p.orbs) {
                                if (o.ID != null && !o.ID.equals(EmptyOrbSlot.ORB_ID) && !types.contains(o.ID))
                                    types.add(o.ID);
                            }
                            int turns = types.size() * _inst.magicNumber;
                            addToTop(new ApplyPowerAction(p, p, new FocusPower(p, 1)));
                            addToTop(new ApplyPowerAction(p, p, new TrancePower(p, turns, 1)));
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}