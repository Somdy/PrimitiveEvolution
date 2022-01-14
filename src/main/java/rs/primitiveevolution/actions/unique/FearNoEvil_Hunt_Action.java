package rs.primitiveevolution.actions.unique;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.AbstractMonster.Intent;


public class FearNoEvil_Hunt_Action extends AbstractGameAction {
    private AbstractPlayer p;
    private AbstractMonster m;
    private DamageInfo info;

    public FearNoEvil_Hunt_Action(AbstractPlayer p, AbstractMonster m, DamageInfo info)
    {
        this.p = p;
        this.m = m;
        this.info = info;
    }

    public void update()
    {
        if ((this.m != null) && ((this.m.intent == Intent.BUFF)
                || (this.m.intent == Intent.DEBUFF)
                || (this.m.intent == Intent.STRONG_DEBUFF)
                || (this.m.intent == AbstractMonster.Intent.ATTACK_BUFF)
                || (this.m.intent == AbstractMonster.Intent.ATTACK_DEBUFF)
                || (this.m.intent == Intent.DEFEND_BUFF)
                || (this.m.intent == Intent.DEFEND_DEBUFF))) {
            addToTop(new DamageAction(this.m,
                    new DamageInfo(p, this.info.output, this.info.type),
                    AbstractGameAction.AttackEffect.SLASH_HEAVY));
        }
        addToTop(new DamageAction(this.m, this.info, AbstractGameAction.AttackEffect.SLASH_HEAVY));
        this.isDone = true;
    }
}
