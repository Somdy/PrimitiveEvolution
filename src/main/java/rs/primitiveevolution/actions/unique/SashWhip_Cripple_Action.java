package rs.primitiveevolution.actions.unique;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.FrailPower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.powers.watcher.MarkPower;

public class SashWhip_Cripple_Action extends AbstractGameAction {
    private AbstractMonster m;
    private int magicNumber;

    public SashWhip_Cripple_Action(AbstractMonster monster, int buffAmount) {
        this.m = monster;
        this.magicNumber = buffAmount;
    }

    @Override
    public void update() {
        if ((AbstractDungeon.actionManager.cardsPlayedThisCombat.size() >= 2) &&
                (((AbstractCard)AbstractDungeon.actionManager.cardsPlayedThisCombat
                        .get(AbstractDungeon.actionManager.cardsPlayedThisCombat
                                .size() - 2)).type == AbstractCard.CardType.ATTACK)) {
            addToTop(new ApplyPowerAction(this.m, AbstractDungeon.player,
                    new WeakPower(this.m, this.magicNumber, false), this.magicNumber));
            addToTop(new ApplyPowerAction(this.m, AbstractDungeon.player,
                    new FrailPower(this.m, this.magicNumber, false), this.magicNumber));
        }
        this.isDone = true;
    }
}
