package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.Bane;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.PoisonPower;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.BetterDamageAllEnemiesAction;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.utility.DamageCallbackBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.*;
import static rs.primitiveevolution.datas.BranchID.Cataclysm;
import static rs.primitiveevolution.datas.BranchID.Scourge;

public class BaneEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return Scourge;
            case 2:
                return Cataclysm;
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
                upgradeEvolvedTexts(card, Scourge);
                setDamage(card, 10);
            });
            add(() -> {
                upgradeEvolvedTexts(card, Cataclysm);
                setDamage(card, 8);
                card.target = AbstractCard.CardTarget.ALL_ENEMY;
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = Bane.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(BaneEvo.class, ctMethodToPatch))
                Nature.Log("Bane is not evolvable.");
        }
    }

    @SpirePatch(clz = Bane.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Bane _inst) {
            if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Bane.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(Bane _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case Scourge:
                        addToBot(new DamageCallbackBuilder(m, new CustomDmgInfo(new DamageSource(p, _inst),
                                _inst.damage, _inst.damageTypeForTurn), SLASH_HORIZONTAL, 
                                c -> {
                                    if (c.hasPower(PoisonPower.POWER_ID)) {
                                        List<AbstractMonster> tmp = LMSK.GetAllExptMstr(mo -> mo != m);
                                        if (!tmp.isEmpty()) {
                                            for (AbstractMonster mo : tmp) {
                                                addToTop(new NullableSrcDamageAction(mo, new CustomDmgInfo(new DamageSource(p, _inst), 
                                                        _inst.damage, _inst.damageTypeForTurn), SLASH_VERTICAL));
                                            }
                                        }
                                    }
                                }));
                        return SpireReturn.Return(null);
                    case Cataclysm:
                        addToBot(new BetterDamageAllEnemiesAction(new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage, _inst.damageTypeForTurn), 
                                LMSK.GetRandom(LMSK.ListFromObjs(SLASH_HORIZONTAL, SLASH_VERTICAL)).orElse(SLASH_HEAVY), true, 
                                c -> {
                                    if (c.hasPower(PoisonPower.POWER_ID)) {
                                        addToTop(new DrawCardAction(p, 1));
                                    }
                                }));
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}