package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FocusPower;
import rs.primitiveevolution.Nature;

public class TrancePower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("Trance");
    
    public TrancePower(AbstractCreature owner, int turns, int focus) {
        super(POWER_ID, "trance", PowerType.DEBUFF, owner);
        setValues(turns, focus);
        updateDescription();
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (!owner.isDeadOrEscaped()) {
            if (amount > 1) 
                addToBot(new ReducePowerAction(owner, owner, this, 1));
            else {
                flash();
                addToBot(new RemoveSpecificPowerAction(owner, owner, this));
                addToBot(new ReducePowerAction(owner, owner, FocusPower.POWER_ID, extraAmt));
            }
        }
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        setAmtValue(1, extraAmt);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new TrancePower(owner, amount, extraAmt);
    }
}