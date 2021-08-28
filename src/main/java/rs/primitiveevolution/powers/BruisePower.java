package rs.primitiveevolution.powers;

import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.primitiveevolution.Nature;

public class BruisePower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("Bruise");
    
    public BruisePower(AbstractCreature owner, int amount) {
        super(POWER_ID, "bruise", PowerType.DEBUFF, owner);
        setValues(amount);
        updateDescription();
    }

    @Override
    public float atDamageFinalReceive(float damage, DamageInfo.DamageType type) {
        if (type == DamageInfo.DamageType.NORMAL && amount > 0) {
            damage += amount * 1.5F;
        }
        return super.atDamageFinalReceive(damage, type);
    }

    @Override
    public void atEndOfRound() {
        if (amount > 0) {
            checkReduction();
            addToBot(new ReducePowerAction(owner, owner, this, extraAmt));
        }
    }
    
    private void checkReduction() {
        extraAmt = amount > 2 ? MathUtils.floor(amount / 3F) : 2;
        setAmtValue(1, extraAmt);
    }

    @Override
    public String preSetDescription() {
        checkReduction();
        float damage = amount * 1.5F;
        setAmtValue(0, damage);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new BruisePower(owner, amount);
    }
}