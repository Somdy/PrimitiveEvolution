package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.primitiveevolution.Nature;

public class ForesightBiPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("ForesightBi");

    public ForesightBiPower(int amount) {
        super(POWER_ID, "gas", PowerType.BUFF, AbstractDungeon.player);
        setValues(amount);
        updateDescription();
    }

    public void atStartOfTurn() {
        if (AbstractDungeon.player.drawPile.size() <= 0) {
            addToTop(new EmptyDeckShuffleAction());
        }
        flash();
        addToBot(new ScryAction(this.amount));
    }

    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer){
            if (AbstractDungeon.player.drawPile.size() <= 0) {
                addToTop(new EmptyDeckShuffleAction());
            }
            flash();
            addToBot(new ScryAction(this.amount));
        }
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new ForesightBiPower(amount);
    }
}
