package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.DaggerThrow;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.ThrowDaggerEffect;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.BaitDagger;
import static rs.primitiveevolution.datas.BranchID.TacticalThrow;

public class DaggerThrowEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return BaitDagger;
            case 2:
                return TacticalThrow;
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
            add(() -> upgradeEvolvedTexts(card, BaitDagger));
            add(() -> {
                upgradeEvolvedTexts(card, TacticalThrow);
                setDamage(card, 10);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = DaggerThrow.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(DaggerThrowEvo.class, ctMethodToPatch))
                Nature.Log("Dagger Throw is not evolvable.");
        }
    }

    @SpirePatch(clz = DaggerThrow.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(DaggerThrow _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = DaggerThrow.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(DaggerThrow _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case BaitDagger:
                        if (m != null) {
                            addToBot(new VFXAction(new ThrowDaggerEffect(m.hb.cX, m.hb.cY)));
                        }
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), 
                                _inst.damage, _inst.damageTypeForTurn)));
                        addToBot(new DrawExptCardAction(p, 1, c -> c.type == AbstractCard.CardType.ATTACK));
                        addToBot(new DiscardAction(p, p, 1, false));
                        return SpireReturn.Return(null);
                    case TacticalThrow:
                        if (m != null) {
                            addToBot(new VFXAction(new ThrowDaggerEffect(m.hb.cX, m.hb.cY)));
                        }
                        addToBot(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn)));
                        addToBot(new DrawExptCardAction(p, 1, c -> c.type == AbstractCard.CardType.SKILL));
                        addToBot(new DiscardAction(p, p, 1, false));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}