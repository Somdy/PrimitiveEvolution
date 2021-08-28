package rs.primitiveevolution.cards.silent;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.DaggerSpray;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.combat.DaggerSprayEffect;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.BetterDamageAllEnemiesAction;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;

import static rs.primitiveevolution.datas.BranchID.AccurateDaggers;
import static rs.primitiveevolution.datas.BranchID.SprayWithShivs;

public class DaggerSprayEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return AccurateDaggers;
            case 2:
                return SprayWithShivs;
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
                upgradeEvolvedTexts(card, AccurateDaggers);
                setDamage(card, 5);
                setMagic(card, 2);
            });
            add(() -> {
                upgradeEvolvedTexts(card, SprayWithShivs);
                setDamage(card, 4);
                card.cardsToPreview = new Shiv();
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = DaggerSpray.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(DaggerSprayEvo.class, ctMethodToPatch))
                Nature.Log("Dagger Spray is not evolvable.");
        }
    }

    @SpirePatch(clz = DaggerSpray.class, method = "upgrade")
    public static class Upgrade {
        @SpirePrefixPatch
        public static SpireReturn Prefix(DaggerSpray _inst) {
            if (_inst instanceof EvolvableCard && !_inst.upgraded) {
                ((EvolvableCard) _inst).possibleBranches().get(((EvolvableCard) _inst).chosenBranch()).upgrade();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = DaggerSpray.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(DaggerSpray _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case AccurateDaggers:
                        for (int i = 0; i < 2; i++) {
                            addToBot(new VFXAction(new DaggerSprayEffect(AbstractDungeon.getMonsters().shouldFlipVfx())));
                            addToBot(new BetterDamageAllEnemiesAction(new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage, 
                                    _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.NONE, true)
                                    .manipulateInfo((c, info) -> {
                                        if (c.powers.stream().anyMatch(po -> po.type == AbstractPower.PowerType.DEBUFF))
                                            info.output += _inst.magicNumber;
                                        return info;
                                    }));
                        }
                        return SpireReturn.Return(null);
                    case SprayWithShivs:
                        for (int i = 0; i < 2; i++) {
                            addToBot(new VFXAction(new DaggerSprayEffect(AbstractDungeon.getMonsters().shouldFlipVfx())));
                            addToBot(new BetterDamageAllEnemiesAction(new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                    _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.NONE, true,
                                    c -> {
                                        if (c.lastDamageTaken > 0) {
                                            Shiv shiv = new Shiv();
                                            shiv.purgeOnUse = true;
                                            addToTop(new NewQueueCardAction(shiv, c, true, true));
                                        }
                                    }));
                        }
                        return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }
}