package rs.primitiveevolution.actions.unique;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;
import rs.lazymankits.abstracts.LMCustomGameAction;
import rs.lazymankits.actions.CustomDmgInfo;
import rs.lazymankits.managers.LMExptMgr;

public class LearnLessonAction extends LMCustomGameAction {
    private String msg;
    
    public LearnLessonAction(AbstractCreature target, CustomDmgInfo info, String msg) {
        this.info = info;
        this.target = target;
        this.msg = msg;
        actionType = ActionType.DAMAGE;
        duration = startDuration = Settings.ACTION_DUR_XFAST;
    }
    
    @Override
    public void update() {
        if (duration == startDuration) {
            if (target == null) {
                isDone = true;
                return;
            }
            effectToList(new FlashAtkImgEffect(target.hb.cX, target.hb.cY, AttackEffect.NONE));
            target.damage(info);
            if (LMExptMgr.FATAL_JUGDE.test(target)) {
                CardGroup tmp = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                cpr().masterDeck.group.stream().filter(AbstractCard::canUpgrade).forEach(tmp::addToRandomSpot);
                if (tmp.isEmpty()) {
                    isDone = true;
                    return;
                }
                AbstractDungeon.gridSelectScreen.open(tmp, 1, msg, true);
            }
            tickDuration();
        }
        if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty() && !AbstractDungeon.isScreenUp) {
            for (AbstractCard card : AbstractDungeon.gridSelectScreen.selectedCards) {
                AbstractDungeon.effectsQueue.add(new UpgradeShineEffect(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                card.upgrade();
                AbstractDungeon.player.bottledCardUpgradeCheck(card);
                AbstractDungeon.effectsQueue.add(new ShowCardBrieflyEffect(card.makeStatEquivalentCopy()));
            }
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
        }
        tickDuration();
    }
}