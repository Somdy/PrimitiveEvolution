package rs.primitiveevolution.powers;

import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.primitiveevolution.Nature;

public class BleedingPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("Bleeding");
    
    public BleedingPower(AbstractCreature owner, int amount) {
        super(POWER_ID, "bleeding", PowerType.DEBUFF, owner);
        setValues(amount);
        updateDescription();
    }

    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (info.owner != owner && info.type == DamageInfo.DamageType.NORMAL && amount > 0) {
            flash();
            addToTop(new LoseHPAction(owner, owner, amount));
            checkReduction();
            addToTop(new ReducePowerAction(owner, owner, this, extraAmt));
        }
        return super.onAttacked(info, damageAmount);
    }
    
    private void checkReduction() {
        extraAmt = amount > 2 ? MathUtils.floor(amount / 3F) : 2;
        setAmtValue(1, extraAmt);
    }

    @Override
    public String preSetDescription() {
        checkReduction();
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new BleedingPower(owner, amount);
    }
}