package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import rs.primitiveevolution.Nature;

public class BreakoutExhaustionPower extends AbstractEvolutionPower{
    public static final String POWER_ID = Nature.MakeID("BreakoutExhaustion");
    
    public BreakoutExhaustionPower(AbstractCreature owner) {
        super(POWER_ID, null, PowerType.BUFF, owner);
        setValues(-1);
        updateDescription();
        loadRegion("darkembrace");
    }
    
    @Override
    public void onAfterUseCard(AbstractCard card, UseCardAction action) {
        if (isCardTypeOf(card, AbstractCard.CardType.ATTACK) && !owner.isDeadOrEscaped()) {
            if (owner.hasPower(StrengthPower.POWER_ID)) {
                owner.powers.stream().filter(p -> p.ID.equals(StrengthPower.POWER_ID))
                        .findFirst()
                        .ifPresent(p -> {
                            flash();
                            addToBot(new ReducePowerAction(owner, owner, p, 1));
                        });
            }
        }
    }
    
    @Override
    public String preSetDescription() {
        return DESCRIPTIONS[0];
    }
    
    @Override
    public AbstractPower makeCopy() {
        return new BreakoutExhaustionPower(owner);
    }
}