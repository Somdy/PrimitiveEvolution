package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import rs.primitiveevolution.Nature;

public class NoxiousDustPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("NoxiousDust");
    
    public NoxiousDustPower(AbstractCreature owner, int amount) {
        super(POWER_ID, "dust", PowerType.BUFF, owner);
        setValues(amount);
        updateDescription();
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (owner.isPlayer && isPlayer && amount > 0 && !areMstrBasicallyDead()) {
            flash();
            for (AbstractMonster m : getAllLivingMstrs()) {
                addToBot(new ApplyPowerAction(m, owner, new PoisonPower(m, owner, amount)));
            }
        }
    }

    @Override
    public AbstractPower makeCopy() {
        return new NoxiousDustPower(owner, amount);
    }
}