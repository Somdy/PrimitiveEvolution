package rs.primitiveevolution.powers;

import basemod.BaseMod;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.unique.ExpertiseAction;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;

public class SkippingPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("Skipping");

    public SkippingPower() {
        super(POWER_ID, null, PowerType.BUFF, LMSK.Player());
        setValues(-1);
        updateDescription();
        loadRegion("ritual");
    }

    @Override
    public void atStartOfTurnPostDraw() {
        if (!owner.isDeadOrEscaped()) {
            flash();
            addToBot(new ExpertiseAction(owner, BaseMod.MAX_HAND_SIZE));
            addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        }
    }

    @Override
    public String preSetDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new SkippingPower();
    }
}