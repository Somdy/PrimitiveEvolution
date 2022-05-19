package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.actions.unique.LikeWaterCalm_Action;

public class LikeWaterCalmPower extends AbstractEvolutionPower{
    public static final String POWER_ID = Nature.MakeID("LikeWaterCalm");

    public LikeWaterCalmPower() {
        super(POWER_ID, "gas", PowerType.BUFF, AbstractDungeon.player);
        setValues(amount);
        updateDescription();
    }

    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer){
            addToBot((AbstractGameAction) new LikeWaterCalm_Action());
        }
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new LikeWaterCalmPower();
    }
}
