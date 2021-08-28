package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.primitiveevolution.Nature;

public class FuryPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("Fury");
    
    public FuryPower(int amount) {
        super(POWER_ID, null, PowerType.BUFF, AbstractDungeon.player);
        setValues(amount);
        updateDescription();
        loadRegion("anger");
    }

    @Override
    public void onAttack(DamageInfo info, int damageAmount, AbstractCreature target) {
        if (info.owner == owner && info.type == DamageInfo.DamageType.NORMAL && amount > 0) {
            flash();
            addToBot(new GainBlockAction(owner, owner, amount));
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
    }

    @Override
    public String preSetDescription() {
        setAmtValue(0, amount);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new FuryPower(amount);
    }
}