package rs.primitiveevolution.actions.unique;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.stances.AbstractStance;

public class Halt_Evo_Action extends AbstractGameAction
{
    private int additionalAmt;

    public Halt_Evo_Action(AbstractCreature target, int block, int additional) {
        this.target = target;
        this.amount = block;
        this.additionalAmt = additional;
    }

    public void update() {
        addToTop(new GainBlockAction(this.target, this.amount));
        if (AbstractDungeon.player.stance.ID.equals("Wrath")) {
            addToTop(new GainBlockAction(this.target, this.additionalAmt));
        }
        this.isDone = true;
    }
}
