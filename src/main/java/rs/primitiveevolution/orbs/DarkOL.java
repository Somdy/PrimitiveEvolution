package rs.primitiveevolution.orbs;

import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.OrbStrings;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Dark;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.vfx.combat.OrbFlareEffect;
import rs.primitiveevolution.utils.EvoImageMst;

public class DarkOL extends Dark {
    public static final String ORB_ID = "DarkOL";
    public static final OrbStrings orbStrings = CardCrawlGame.languagePack.getOrbString("Dark");
    public static final String[] MSG = orbStrings.DESCRIPTION;
    
    public DarkOL(int baseEvoke, int basePassive) {
        ID = ORB_ID;
        name = orbStrings.NAME;
        img = EvoImageMst.Dark;
        evokeAmount = baseEvokeAmount = baseEvoke;
        passiveAmount = basePassiveAmount = basePassive;
        updateDescription();
        channelAnimTimer = 0.5F;
    }
    
    public DarkOL() {
        this(6, 6);
    }
    
    @Override
    public void updateDescription() {
        applyFocus();
        description = MSG[0] + MathUtils.floor(passiveAmount * 1.25F) + MSG[1] + evokeAmount + MSG[2];
    }

    @Override
    public void onEndOfTurn() {
        float sT = Settings.FAST_MODE ? 0F : 0.6F / cpr().orbs.size();
        addToBot(new VFXAction(new OrbFlareEffect(this, OrbFlareEffect.OrbFlareColor.DARK), sT));
        evokeAmount += MathUtils.floor(passiveAmount * 1.25F);
        updateDescription();
    }

    @Override
    public void applyFocus() {
        AbstractPower power = cpr().getPower(FocusPower.POWER_ID);
        if (power != null) {
            this.passiveAmount = Math.max(0, this.basePassiveAmount + power.amount);
        } else {
            this.passiveAmount = this.basePassiveAmount;
        }
    }

    @Override
    public AbstractOrb makeCopy() {
        return new DarkOL(baseEvokeAmount, basePassiveAmount);
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