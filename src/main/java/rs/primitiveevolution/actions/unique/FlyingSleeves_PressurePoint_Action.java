package rs.primitiveevolution.actions.unique;

import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.watcher.MarkPower;
import com.megacrit.cardcrawl.vfx.WallopEffect;
import com.megacrit.cardcrawl.vfx.combat.AnimatedSlashEffect;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;

public class FlyingSleeves_PressurePoint_Action extends AbstractGameAction {

    private DamageInfo info;

    public FlyingSleeves_PressurePoint_Action(AbstractCreature target, DamageInfo info)
    {
        this.info = info;
        setValues(target, info);
        this.actionType = AbstractGameAction.ActionType.DAMAGE;
        this.startDuration = Settings.ACTION_DUR_FAST;
        this.duration = this.startDuration;
    }

    public void update()
    {
        if (shouldCancelAction())
        {
            this.isDone = true;
            return;
        }
        tickDuration();
        if (this.isDone)
        {
            if (target != null)
            {
                addToBot(new SFXAction("ATTACK_WHIFF_2", 0.3F));
                addToBot(new SFXAction("ATTACK_FAST", 0.2F));
                addToBot(new VFXAction(new AnimatedSlashEffect(target.hb.cX, target.hb.cY - 30.0F * Settings.scale,
                        500.0F, 200.0F, 290.0F, 3.0F, Color.VIOLET, Color.PINK)));
            }
            addToTop(new DamageAction(target, info));
            if (this.target.lastDamageTaken > 0)
                addToBot(new ApplyPowerAction(target, this.source,
                        new MarkPower(target, this.target.lastDamageTaken), this.target.lastDamageTaken));
            if (target != null)
            {
                addToBot(new SFXAction("ATTACK_WHIFF_1", 0.2F));
                addToBot(new SFXAction("ATTACK_FAST", 0.2F));
                addToBot(new VFXAction(new AnimatedSlashEffect(target.hb.cX, target.hb.cY - 30.0F * Settings.scale,
                        500.0F, -200.0F, 250.0F, 3.0F, Color.VIOLET, Color.PINK)));
            }
            addToTop(new DamageAction(target, info));
            if (this.target.lastDamageTaken > 0)
                addToBot(new ApplyPowerAction(target, this.source,
                        new MarkPower(target, this.target.lastDamageTaken), this.target.lastDamageTaken));
        }
    }
}
