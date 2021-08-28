package rs.primitiveevolution.orbs;

import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.OrbStrings;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Lightning;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FocusPower;
import rs.primitiveevolution.utils.EvoImageMst;

public class LightningOL extends Lightning {
    public static final String ORB_ID = "LightningOL";
    public static final OrbStrings orbStrings = CardCrawlGame.languagePack.getOrbString("Lightning");
    public static final String[] MSG = orbStrings.DESCRIPTION;
    
    public LightningOL(int baseEvoke, int basePassive) {
        ID = ORB_ID;
        img = EvoImageMst.Lightning;
        name = orbStrings.NAME;
        evokeAmount = baseEvokeAmount = baseEvoke;
        passiveAmount = basePassiveAmount = basePassive;
        updateDescription();
        angle = MathUtils.random(360.0F);
        channelAnimTimer = 0.5F;
    }
    
    public LightningOL() {
        this(8, 3);
    }
    
    @Override
    public void updateDescription() {
        applyFocus();
        description = MSG[0] + passiveAmount + MSG[1] + evokeAmount + MSG[2];
    }

    @Override
    public void applyFocus() {
        AbstractPower power = cpr().getPower(FocusPower.POWER_ID);
        if (power != null) {
            passiveAmount = Math.max(0, basePassiveAmount + MathUtils.floor(power.amount * 1.5F));
            evokeAmount = Math.max(0, baseEvokeAmount + MathUtils.floor(power.amount * 1.5F));
        } else {
            passiveAmount = basePassiveAmount;
            evokeAmount = baseEvokeAmount;
        }
    }

    @Override
    public AbstractOrb makeCopy() {
        return new LightningOL(baseEvokeAmount, basePassiveAmount);
    }

    protected void addToBot(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }

    protected void addToTop(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToTop(action);
    }

    protected AbstractPlayer cpr() {
        return AbstractDungeon.player;
    }
}