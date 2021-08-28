package rs.primitiveevolution.orbs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.OrbStrings;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Frost;
import rs.primitiveevolution.utils.EvoImageMst;

public class FrostOL extends Frost {
    public static final String ORB_ID = "FrostOL";
    public static final OrbStrings orbStrings = CardCrawlGame.languagePack.getOrbString("Frost");
    public static final String[] MSG = orbStrings.DESCRIPTION;
    private boolean hFlip1;
    private boolean hFlip2;
    
    public FrostOL(int baseEvoke, int basePassive) {
        ID = ORB_ID;
        name = orbStrings.NAME;
        evokeAmount = baseEvokeAmount = baseEvoke;
        passiveAmount = basePassiveAmount = basePassive;
        updateDescription();
        channelAnimTimer = 0.5F;
        hFlip1 = MathUtils.randomBoolean();
        hFlip2 = MathUtils.randomBoolean();
    }
    
    public FrostOL() {
        this(5, 2);
    }
    
    @Override
    public void updateDescription() {
        applyFocus();
        description = MSG[0] + passiveAmount + MSG[1] + evokeAmount + MSG[2];
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setColor(this.c);
        sb.draw(EvoImageMst.Frost_Right, this.cX - 48.0F + this.bobEffect.y / 4.0F, this.cY - 48.0F + this.bobEffect.y / 4.0F, 
                48.0F, 48.0F, 96.0F, 96.0F, this.scale, this.scale, 0.0F, 
                0, 0, 96, 96, this.hFlip1, false);
        sb.draw(EvoImageMst.Frost_Left, this.cX - 48.0F + this.bobEffect.y / 4.0F, this.cY - 48.0F - this.bobEffect.y / 4.0F, 
                48.0F, 48.0F, 96.0F, 96.0F, this.scale, this.scale, 0.0F, 
                0, 0, 96, 96, this.hFlip1, false);
        sb.draw(EvoImageMst.Frost_Mid, this.cX - 48.0F - this.bobEffect.y / 4.0F, this.cY - 48.0F + this.bobEffect.y / 2.0F, 
                48.0F, 48.0F, 96.0F, 96.0F, this.scale, this.scale, 0.0F, 
                0, 0, 96, 96, this.hFlip2, false);
        renderText(sb);
        hb.render(sb);
    }

    @Override
    public void playChannelSFX() {
        super.playChannelSFX();
        addToTop(new GainBlockAction(cpr(), cpr(), passiveAmount));
    }

    @Override
    public AbstractOrb makeCopy() {
        return new FrostOL(baseEvokeAmount, basePassiveAmount);
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