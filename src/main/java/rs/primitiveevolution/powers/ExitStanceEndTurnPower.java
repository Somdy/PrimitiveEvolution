package rs.primitiveevolution.powers;

import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.actions.watcher.NotStanceCheckAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.stances.NeutralStance;
import com.megacrit.cardcrawl.vfx.combat.EmptyStanceEffect;
import rs.primitiveevolution.Nature;

public class ExitStanceEndTurnPower extends AbstractEvolutionPower {
    public static final String POWER_ID = Nature.MakeID("ExitStanceEndTurn");
    private String stanceName;
    private String stanceID;
    
    public ExitStanceEndTurnPower() {
        super(POWER_ID, null, PowerType.BUFF, AbstractDungeon.player);
        setValues(-1);
        updateDescription();
        loadRegion("int");
    }

    @Override
    public void onInitialApplication() {
        if (cpr().stance != null || !cpr().stance.ID.equals(NeutralStance.STANCE_ID)) {
            stanceName = cpr().stance.name;
            stanceID = cpr().stance.ID;
            setCrtName(0, stanceName);
            this.ID = this.ID + stanceName;
            updateDescription();
        }
        if (!owner.isPlayer)
            addToTop(new RemoveSpecificPowerAction(owner, owner, this));
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (cpr().stance.ID.equals(stanceID)) {
            flash();
            addToBot(new NotStanceCheckAction("Neutral", 
                    new VFXAction(new EmptyStanceEffect(owner.hb.cX, owner.hb.cY), 0.1F)));
            addToBot(new ChangeStanceAction("Neutral"));
        }
        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
    }

    @Override
    public String preSetDescription() {
        if (stanceName != null)
            setCrtName(0, stanceName);
        return DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new ExitStanceEndTurnPower();
    }
}