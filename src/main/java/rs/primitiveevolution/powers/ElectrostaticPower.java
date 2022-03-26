package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.defect.EvokeOrbAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.actions.ChannelOverloadedOrbAction;

public class ElectrostaticPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("Electrostatic");
    
    public ElectrostaticPower(AbstractCreature owner, int amount) {
        super(POWER_ID, null, PowerType.BUFF, owner);
        setValues(amount);
        updateDescription();
        loadRegion("static_discharge");
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (info.owner != owner && info.owner != null && info.type == DamageInfo.DamageType.NORMAL && damageAmount > 0) {
            flash();
            addToBot(new EvokeOrbAction(1));
            for (int i = 0; i < amount; i++) {
                addToBot(new ChannelOverloadedOrbAction(ChannelOverloadedOrbAction.OrbType.Lightning));
            }
        }
        return super.onAttacked(info, damageAmount);
    }
    
    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new ElectrostaticPower(owner, amount);
    }
}