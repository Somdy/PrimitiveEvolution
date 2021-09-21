package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import rs.primitiveevolution.Nature;

public class NoxiousGasPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("NoxiousGas");
    
    public NoxiousGasPower(AbstractCreature owner, int poisonAmt, int times) {
        super(POWER_ID, "gas", PowerType.BUFF, owner);
        setValues(poisonAmt, times);
        updateDescription();
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        setAmtValue(1, extraAmt);
        return DESCRIPTIONS[0];
    }

    @Override
    public void atStartOfTurnPostDraw() {
        if (amount > 0 && extraAmt > 0) {
            flash();
            for (int i = 0; i < extraAmt; i++) {
                AbstractMonster m = AbstractDungeon.getRandomMonster();
                addToBot(new ApplyPowerAction(m, owner, new PoisonPower(m, owner, amount)));
            }
        }
    }

    @Override
    public AbstractPower makeCopy() {
        return new NoxiousGasPower(owner, amount, extraAmt);
    }
}