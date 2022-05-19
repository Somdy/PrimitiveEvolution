package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.primitiveevolution.Nature;

import static com.megacrit.cardcrawl.cards.DamageInfo.DamageType.NORMAL;

public class LikeWaterEmptyPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("LikeWaterEmpty");

    public LikeWaterEmptyPower(int amount) {
        super(POWER_ID, "gas", PowerType.BUFF, AbstractDungeon.player);
        setValues(amount);
        updateDescription();
    }

    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer){
            addToBot(new DamageAllEnemiesAction(AbstractDungeon.player, amount, NORMAL,
                    AbstractGameAction.AttackEffect.NONE));
        }
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new LikeWaterEmptyPower(amount);
    }
}
