package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.stances.AbstractStance;
import rs.primitiveevolution.Nature;

public class Rushdown_FreeFlow_Power extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("FreeFlow");

    public Rushdown_FreeFlow_Power(int amount) {
        super(POWER_ID, "gas", PowerType.BUFF, AbstractDungeon.player);
        updateDescription();
    }

    public void onChangeStance(AbstractStance oldStance, AbstractStance newStance) {
        flash();
        addToBot(new DrawCardAction(this.owner, this.amount));
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new Rushdown_FreeFlow_Power(amount);
    }
}
