package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.actions.utility.QuickAction;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;

public class PreparationReturnPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("PreparationReturn");
    private final AbstractCard cardToReturn;
    
    public PreparationReturnPower(AbstractCard cardToReturn) {
        super(POWER_ID, null, PowerType.BUFF, LMSK.Player());
        this.cardToReturn = cardToReturn;
        ID += cardToReturn.uuid;
        setValues(-1);
        updateDescription();
        loadRegion("rebound");
    }
    
    @Override
    public void atStartOfTurn() {
        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        if (cpr().discardPile.group.stream().anyMatch(c -> c == cardToReturn)) {
            flash();
            addToBot(new QuickAction(() -> cpr().discardPile.moveToHand(cardToReturn)));
        } else if (cpr().drawPile.group.stream().anyMatch(c -> c == cardToReturn)) {
            flash();
            addToBot(new QuickAction(() -> cpr().drawPile.moveToHand(cardToReturn)));
        }
    }
    
    @Override
    public String preSetDescription() {
        setCrtName(0, cardToReturn.name);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new PreparationReturnPower(cardToReturn);
    }
}