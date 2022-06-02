package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.powers.AbstractPower;
import rs.lazymankits.utils.LMSK;
import rs.primitiveevolution.Nature;

public class SketchingPower extends AbstractEvolutionPower{
    public static final String POWER_ID = Nature.MakeID("Sketching");

    public SketchingPower() {
        super(POWER_ID, null, PowerType.BUFF, LMSK.Player());
        setValues(-1);
        updateDescription();
        loadRegion("like_water");
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (!owner.isDeadOrEscaped()) {
            flashWithoutSound();
            addToBot(new DrawCardAction(1, new AbstractGameAction() {
                @Override
                public void update() {
                    isDone = true;
                    for (AbstractCard c : DrawCardAction.drawnCards)
                        c.setCostForTurn(getCardRealCost(c) - 1);
                }
            }));
        }
    }
    
    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer && owner.isPlayer) {
            addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        }
    }
    
    @Override
    public String preSetDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new SketchingPower();
    }
}
