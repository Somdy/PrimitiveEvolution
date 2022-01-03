package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.primitiveevolution.Nature;

public class ProtectionPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("Protection");
    private boolean temp;
    
    public ProtectionPower(AbstractCreature owner, int amount, boolean temp) {
        super(POWER_ID, null, PowerType.DEBUFF, owner);
        this.temp = temp;
        ID += this.temp;
        setValues(amount);
        updateDescription();
        loadRegion("channel");
    }
    
    public ProtectionPower(AbstractCreature owner, int amount) {
        this(owner, amount, false);
    }
    
    @Override
    public float atDamageFinalReceive(float damage, DamageInfo.DamageType type) {
        damage -= amount;
        return super.atDamageFinalReceive(damage, type);
    }
    
    @Override
    public void atEndOfRound() {
        if (temp)
            addToBot(new RemoveSpecificPowerAction(owner, owner, this));
    }
    
    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return (temp ? DESCRIPTIONS[1] : "") + DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new ProtectionPower(owner, amount, temp);
    }
}