package rs.primitiveevolution.actions.unique;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;

public class BowlingBash_Decompress_Action extends AbstractGameAction {
    private int energyGainAmt;
    private DamageInfo info;
    private int attackCount;

    public BowlingBash_Decompress_Action(AbstractCreature target, DamageInfo info, int energyAmt, int attackCount)
    {
        this.info = info;
        setValues(target, info);
        this.energyGainAmt = energyAmt;
        this.actionType = AbstractGameAction.ActionType.DAMAGE;
        this.duration = Settings.ACTION_DUR_FASTER;
        this.attackCount = attackCount;
    }

    @Override
    public void update() {
        if ((this.duration == Settings.ACTION_DUR_FASTER) &&
                (this.target != null))
        {
            AbstractDungeon.effectList.add(new FlashAtkImgEffect(this.target.hb.cX,
                    this.target.hb.cY, AbstractGameAction.AttackEffect.BLUNT_HEAVY));

            for ( int i = 0; i < this.attackCount; i ++ ) {
                this.target.damage(this.info);
            }
            if ((((AbstractMonster)this.target).isDying) || (this.target.currentHealth <= 0)) {
                addToBot(new GainEnergyAction(this.energyGainAmt));
            }
            if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                AbstractDungeon.actionManager.clearPostCombatActions();
            }
        }
        tickDuration();
    }
}
