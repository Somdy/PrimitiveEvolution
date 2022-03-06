package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.watcher.MarkPower;
import rs.primitiveevolution.Nature;

public class AddPressurePointPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("PressurePointed");

    private final AbstractPlayer p;
    public AddPressurePointPower(AbstractCreature owner, AbstractPlayer p, int amount) {
        super(POWER_ID, "bruise", PowerType.DEBUFF, owner);
        this.p = p;
        setValues(amount);
        updateDescription();
    }

    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (info.owner != owner && info.type == DamageInfo.DamageType.NORMAL && amount > 0) {
            flash();
            addToTop(new ApplyPowerAction(owner, p, new MarkPower(owner, amount), amount));
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
        return new AddPressurePointPower(owner, p, amount);
    }
}
