package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.interfaces.utilities.DrawCardAmountModifier;
import rs.primitiveevolution.Nature;

public class MachineResearchPower extends AbstractEvolutionPower implements DrawCardAmountModifier {
    public static final String POWER_ID = Nature.MakeID("MachineResearch");
    private int cardsDrawn;
    
    public MachineResearchPower(int amount) {
        super(POWER_ID, "research", PowerType.BUFF, AbstractDungeon.player);
        setValues(amount);
        updateDescription();
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        if (amount <= 0 || !owner.isPlayer)
            addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        return DESCRIPTIONS[0];
    }

    @Override
    public void onInitialApplication() {
        cardsDrawn = cpr().gameHandSize;
    }

    @Override
    public void atStartOfTurn() {
        if (!owner.isDeadOrEscaped()) {
            cardsDrawn = 0;
        }
    }

    @Override
    public void onCardDraw(AbstractCard card) {
        if (!AbstractDungeon.actionManager.turnHasEnded) 
            cardsDrawn++;
    }

    @Override
    public AbstractPower makeCopy() {
        return new MachineResearchPower(amount);
    }

    @Override
    public int modifyDrawAmount(AbstractCreature source, int drawAmt, boolean endTurnDraw) {
        if (cardsDrawn >= cpr().gameHandSize && !endTurnDraw && owner.isPlayer && amount > 0
                && !AbstractDungeon.actionManager.turnHasEnded) {
            flash();
            drawAmt += amount;
        }
        return drawAmt;
    }
}