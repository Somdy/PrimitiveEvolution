package rs.primitiveevolution.cards.ironclad;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.cards.red.SwordBoomerang;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.watcher.VigorPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.Sys;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.utility.DamageCallbackBuilder;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;
import rs.primitiveevolution.powers.BoomerangReturnPower;
import rs.primitiveevolution.powers.BruisePower;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;
import static rs.primitiveevolution.datas.BranchID.*;

public class SwordBoomerangEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return ManueverBoomerang;
            case 2:
                return SplitSwords;
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
                upgradeEvolvedTexts(card, ManueverBoomerang);
                setDamage(card, 4);
                setMagic(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, SplitSwords);
                setDamage(card, 5);
                setMagic(card, 3);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = SwordBoomerang.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws Exception {
            if (!manipulateMethods(SwordBoomerangEvo.class, ctMethodToPatch))
                Nature.Log("Sword Boomerang is not evolvable.");
        }
    }

    @SpirePatch(clz = SwordBoomerang.class, method = "upgrade")
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

    @SpirePatch(clz = SwordBoomerang.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case ManueverBoomerang:
                        addToBot(new QuickAction(() -> {
                            for (int i = 0; i < _inst.magicNumber; i++) {
                                Optional<AbstractMonster> mo = LMSK.GetExptMstr(c -> true);
                                mo.ifPresent(c -> addToTop(new NullableSrcDamageAction(c, new CustomDmgInfo(new DamageSource(p, _inst),
                                        _inst.damage, _inst.damageTypeForTurn), SLASH_HORIZONTAL)));
                            }
                        }));
                        addToBot(new ApplyPowerAction(p, p, new BoomerangReturnPower(_inst)));
                        return SpireReturn.Return(null);
                    case SplitSwords:
                        addToBot(new QuickAction(() -> {
                            for (int i = 0; i < _inst.magicNumber; i++) {
                                Optional<AbstractMonster> mo = LMSK.GetExptMstr(c -> true);
                                mo.ifPresent(c -> addToTop(new DamageCallbackBuilder(c, new CustomDmgInfo(new DamageSource(p, _inst),
                                        _inst.damage, _inst.damageTypeForTurn), SLASH_HORIZONTAL, 
                                        crt -> addToTop(new ApplyPowerAction(crt, p, new BruisePower(crt, 4))))));
                            }
                        }));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}