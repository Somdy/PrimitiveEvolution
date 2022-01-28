package rs.primitiveevolution.cards.defect;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.blue.FTL;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtBehavior;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.actions.DamageSource;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.lazymankits.actions.tools.GridCardManipulator;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.actions.utility.SimpleGridCardSelectBuilder;
import rs.lazymankits.interfaces.cards.UpgradeBranch;
import rs.lazymankits.listeners.DrawCardListener;
import rs.lazymankits.patches.branchupgrades.BranchableUpgradePatch;
import rs.lazymankits.patches.branchupgrades.HandCardSelectFixPatch;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.megacrit.cardcrawl.cards.AbstractCard.CardRarity.*;
import static rs.primitiveevolution.datas.BranchID.*;

public class FTLEvo extends Evolution {

    public static int branchID(AbstractCard card) {
        int chosenBranch = -1;
        if (card instanceof EvolvableCard)
            chosenBranch = ((EvolvableCard) card).chosenBranch();
        switch (chosenBranch) {
            case 1:
                return RelativeTouch;
            case 2:
                return BreakTheReality;
            case 3:
                return BackToBack;
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
                upgradeDamage(card, 1);
                upgradeMagic(card, 1);
            });
            add(() -> {
                upgradeEvolvedTexts(card, RelativeTouch);
                setMagic(card, 3);
            });
            add(() -> {
                upgradeEvolvedTexts(card, BreakTheReality);
                setMagic(card, 5);
            });
            add(() -> {
                upgradeEvolvedTexts(card, BackToBack);
                setMagic(card, 4);
            });
        }};
    }

    public static void upgrade(AbstractCard card) {
        if (card instanceof EvolvableCard)
            ((EvolvableCard) card).possibleBranches().get(((EvolvableCard) card).chosenBranch()).upgrade();
    }

    @SpirePatch(clz = FTL.class, method = SpirePatch.CONSTRUCTOR)
    public static class Evolve {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) {
            if (!manipulateMethods(FTLEvo.class, ctMethodToPatch))
                Nature.Log("FTL is not evolvable.");
        }
    }

    @SpirePatch(clz = FTL.class, method = "upgrade")
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

    @SpirePatch(clz = FTL.class, method = "use")
    public static class Use {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _inst, AbstractPlayer p, AbstractMonster m) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                switch (((EvolvableCard) _inst).evolanch()) {
                    case RelativeTouch:
                        addToBot(new QuickAction(() -> {
                            addToTop(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage, 
                                    _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                            if (AbstractDungeon.actionManager.cardsPlayedThisTurn.size() - 1 < _inst.magicNumber) {
                                for (int i = 0; i < 2; i++) {
                                    if (!p.drawPile.isEmpty()) {
                                        Optional<AbstractCard> opt = LMSK.GetRandom(p.drawPile.group, LMSK.CardRandomRng());
                                        opt.ifPresent(card -> {
                                            p.drawPile.removeCard(card);
                                            addToTop(new NewQueueCardAction(card, AbstractDungeon.getRandomMonster(),
                                                    true, true));
                                        });
                                    }
                                }
                            }
                        }));
                        return SpireReturn.Return();
                    case BreakTheReality:
                        addToBot(new QuickAction(() -> {
                            addToTop(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                    _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                            if (AbstractDungeon.actionManager.cardsPlayedThisTurn.size() - 1 < _inst.magicNumber) {
                                CardGroup tmp = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                                while (tmp.size() < 5) {
                                    Optional<AbstractCard.CardRarity> rarity = LMSK.GetRandom(LMSK.ListFromObjs(COMMON, UNCOMMON, RARE), LMSK.CardRandomRng());
                                    AbstractCard card = CardLibrary.getAnyColorCard(rarity.orElse(UNCOMMON));
                                    tmp.addToRandomSpot(card);
                                }
                                addToTop(new SimpleGridCardSelectBuilder(c -> true)
                                        .setAmount(1)
                                        .setCardGroup(tmp)
                                        .setCanCancel(false)
                                        .setAnyNumber(false)
                                        .setMsg(getGridToHandSelectUI(1, false))
                                        .setManipulator(new GridCardManipulator() {
                                            @Override
                                            public boolean manipulate(AbstractCard card, int index, CardGroup where) {
                                                addToTop(new MakeTempCardInHandAction(card.makeStatEquivalentCopy(), 1));
                                                return false;
                                            }
                                        })
                                );
                            }
                        }));
                        return SpireReturn.Return();
                    case BackToBack:
                        addToBot(new QuickAction(() -> {
                            addToTop(new NullableSrcDamageAction(m, new CustomDmgInfo(new DamageSource(p, _inst), _inst.damage,
                                    _inst.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                            if (AbstractDungeon.actionManager.cardsPlayedThisTurn.size() - 1 < _inst.magicNumber) {
                                if (DrawCardListener.CardsDrawnLastTurn.isEmpty()) return;
                                CardGroup tmp = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                                DrawCardListener.CardsDrawnLastTurn.forEach(c -> tmp.addToRandomSpot(c.makeStatEquivalentCopy()));
                                addToTop(new SimpleGridCardSelectBuilder(c -> true)
                                        .setAmount(2)
                                        .setCardGroup(tmp)
                                        .setCanCancel(true)
                                        .setAnyNumber(true)
                                        .setMsg(getGridToHandSelectUI(2, true))
                                        .setManipulator(new GridCardManipulator() {
                                            @Override
                                            public boolean manipulate(AbstractCard card, int index, CardGroup where) {
                                                AbstractCard copy = card.makeStatEquivalentCopy();
                                                copy.exhaust = true;
                                                addToTop(new MakeTempCardInHandAction(copy, 1));
                                                return false;
                                            }
                                        })
                                );
                            }
                        }));
                        return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = FTL.class, method = "applyPowers")
    public static class ApplyPowers {
        @SpireInsertPatch(rloc = 2)
        public static SpireReturn Insert(AbstractCard _inst) {
            if (BranchableUpgradePatch.OptFields.SelectingBranch.get(AbstractDungeon.gridSelectScreen)
                    || HandCardSelectFixPatch.HandOptFields.SelectingBranch.get(AbstractDungeon.handCardSelectScreen)) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
        @SpireInsertPatch(rloc = 5)
        public static SpireReturn Insert2(AbstractCard _inst) {
            if (_inst instanceof EvolvableCard && _inst.upgraded
                    && !BranchableUpgradePatch.OptFields.SelectingBranch.get(AbstractDungeon.gridSelectScreen)
                    && !HandCardSelectFixPatch.HandOptFields.SelectingBranch.get(AbstractDungeon.handCardSelectScreen)) {
                if (((EvolvableCard) _inst).evolanch() != 0) {
                    _inst.rawDescription = ((EvolvableCard) _inst).getEvolvedText(((EvolvableCard) _inst).evolanch());
                }
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = FTL.class, method = "onMoveToDiscard")
    public static class OnMoveToDiscard {
        @SpirePostfixPatch
        public static void Postfix(AbstractCard _inst) {
            if (_inst instanceof EvolvableCard && _inst.upgraded) {
                _inst.rawDescription = ((EvolvableCard) _inst).getEvolvedText(((EvolvableCard) _inst).evolanch());
                _inst.initializeDescription();
            }
        }
    }
}