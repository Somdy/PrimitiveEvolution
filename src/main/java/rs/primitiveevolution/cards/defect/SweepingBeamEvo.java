package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.SweepingBeam;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.LockOnPower;
import com.megacrit.cardcrawl.vfx.combat.SweepingBeamEffect;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.BetterDamageAllEnemiesAction;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.MarkBeam;
import static rs.primitiveevolution.datas.BranchID.SweepingLaser;

public class SweepingBeamEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return MarkBeam;
            case 2:
                return SweepingLaser;
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
                upgradeEvolvedTexts(card, MarkBeam);
                setMagic(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, SweepingLaser);
                setMagic(card, 2);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = SweepingBeam.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(SweepingBeamEvo.class, ctMethodToPatch))
                Nature.Log("Sweeping Beam is not evolvable.");
        }
    }

    @SpirePatch(clz = SweepingBeam.class, method = "upgrade")
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

    @SpirePatch(clz = SweepingBeam.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case MarkBeam:
                        addToBot(new SFXAction("ATTACK_DEFECT_BEAM"));
                        addToBot(new VFXAction(p, new SweepingBeamEffect(p.hb.cX, p.hb.cY, p.flipHorizontal), 0.25F));
                        addToBot(new BetterDamageAllEnemiesAction(new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage, 
                                _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.FIRE, true, c -> {
                            if (c.lastDamageTaken > 0)
                                addToTop(new ApplyPowerAction(c, p, new LockOnPower(c, _inst.magicNumber)));
                        }));
                        return SpireReturn.Return();
                    case SweepingLaser:
                        addToBot(new SFXAction("ATTACK_DEFECT_BEAM"));
                        addToBot(new VFXAction(p, new SweepingBeamEffect(p.hb.cX, p.hb.cY, p.flipHorizontal), 0.25F));
                        addToBot(new BetterDamageAllEnemiesAction(new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.FIRE, true));
                        addToBot(new DrawExptCardAction(p, _inst.magicNumber, c -> c.type == AbstractCard.CardType.SKILL && c.costForTurn == 1));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}