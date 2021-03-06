package rs.primitiveevolution.actions.unique;

import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.combat.MiracleEffect;

public class FollowUp_Lightening_Action extends AbstractGameAction {
    @Override
    public void update() {
        if ((AbstractDungeon.actionManager.cardsPlayedThisCombat.size() >= 2) &&
                (((AbstractCard)AbstractDungeon.actionManager.cardsPlayedThisCombat
                        .get(AbstractDungeon.actionManager.cardsPlayedThisCombat
                        .size() - 2)).type == AbstractCard.CardType.ATTACK))
        {
            addToTop(new GainEnergyAction(1));
            addToTop(new DrawCardAction(1));
            if (Settings.FAST_MODE) {
                addToTop(new VFXAction(
                        new MiracleEffect(Color.CYAN, Color.PURPLE, "ATTACK_MAGIC_SLOW_1"), 0.0F));
            }
            else {
                addToTop(new VFXAction(
                        new MiracleEffect(Color.CYAN, Color.PURPLE, "ATTACK_MAGIC_SLOW_1"), 0.3F));
            }
        }
        this.isDone = true;
    }
}
