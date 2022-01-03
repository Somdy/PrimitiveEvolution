package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;

public class BoomerangReturnPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("BoomerangReturn");
    private final AbstractCard boomerang;
    
    public BoomerangReturnPower(AbstractCard boomerang) {
        super(POWER_ID, null, PowerType.BUFF, LMSK.Player());
        this.boomerang = boomerang;
        ID += boomerang.uuid;
        setValues(-1);
        updateDescription();
        loadRegion("rebound");
    }

    @Override
    public void atStartOfTurnPostDraw() {
        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        if (cpr().discardPile.group.stream().anyMatch(c -> c == boomerang)) {
            flash();
            addToBot(new QuickAction(() -> cpr().discardPile.moveToHand(boomerang)));
        }
    }

    @Override
    public String preSetDescription() {
        setCrtName(0, boomerang.name);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new BoomerangReturnPower(boomerang);
    }
}