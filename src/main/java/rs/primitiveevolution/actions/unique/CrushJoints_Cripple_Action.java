package rs.primitiveevolution.actions.unique;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;

public class CrushJoints_Cripple_Action extends AbstractGameAction {
    private AbstractMonster m;
    private AbstractPlayer p;
    private int magicNumber;

    public CrushJoints_Cripple_Action(AbstractMonster monster, AbstractPlayer player, int buffAmount)
    {
        this.m = monster;
        this.p = player;
        this.magicNumber = buffAmount;
    }

    @Override
    public void update() {
        if ((AbstractDungeon.actionManager.cardsPlayedThisCombat.size() >= 2) &&
                (((AbstractCard)AbstractDungeon.actionManager.cardsPlayedThisCombat
                        .get(AbstractDungeon.actionManager.cardsPlayedThisCombat.size() - 2))
                        .type == AbstractCard.CardType.SKILL)) {
            addToTop(new ApplyPowerAction(this.m, AbstractDungeon.player,
                    new VulnerablePower(this.m, this.magicNumber, false), this.magicNumber));
            addToTop(new ApplyPowerAction(m, p, new StrengthPower(m, -this.magicNumber), -this.magicNumber));
        }
        this.isDone = true;
    }
}
