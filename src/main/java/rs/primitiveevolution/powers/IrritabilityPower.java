package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.common.DrawExptCardAction;
import rs.primitiveevolution.Nature;

public class IrritabilityPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("Irritability");
    
    public IrritabilityPower(int amount) {
        super(POWER_ID, null, PowerType.BUFF, AbstractDungeon.player);
        setValues(amount);
        updateDescription();
        loadRegion("anger");
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (isCardTypeOf(card, AbstractCard.CardType.ATTACK) && amount > 0) {
            flash();
            addToBot(new DrawExptCardAction(owner, amount, c -> isCardTypeOf(c, AbstractCard.CardType.ATTACK) && c != card)
                    .discardPileNotIncluded());
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new IrritabilityPower(amount);
    }
}