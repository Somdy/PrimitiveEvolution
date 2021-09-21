package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.common.NullableSrcDamageAction;
import rs.primitiveevolution.Nature;

public class DamageNextTurnPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("DamageNextTurn");
    
    public DamageNextTurnPower(AbstractCreature owner, AbstractCreature source, int damage) {
        super(POWER_ID, null, PowerType.DEBUFF, owner);
        setValues(source, damage);
        updateDescription();
        loadRegion("master_smite");
    }

    @Override
    public void atEndOfRound() {
        if (owner != null && !owner.isDeadOrEscaped()) {
            flash();
            addToBot(new NullableSrcDamageAction(owner, crtDmgInfo(source, amount, DamageInfo.DamageType.NORMAL), 
                    AbstractGameAction.AttackEffect.BLUNT_HEAVY));
            addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        }
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new DamageNextTurnPower(owner, source, amount);
    }
}