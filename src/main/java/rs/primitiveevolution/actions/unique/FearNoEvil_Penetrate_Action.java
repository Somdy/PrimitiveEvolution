package rs.primitiveevolution.actions.unique;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.AbstractMonster.Intent;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;


public class FearNoEvil_Penetrate_Action extends AbstractGameAction {
        private final int amount = 2;
        private AbstractPlayer p;
        private AbstractMonster m;
        private DamageInfo info;

        public FearNoEvil_Penetrate_Action(AbstractPlayer p, AbstractMonster m, DamageInfo info)
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
                addToTop(new ApplyPowerAction(p, p, new StrengthPower(p, this.amount), this.amount));
                addToTop(new ApplyPowerAction(p, p, new DexterityPower(p, this.amount), this.amount));
            }
            addToTop(new DamageAction(this.m, this.info, AbstractGameAction.AttackEffect.SLASH_HEAVY));
            this.isDone = true;
        }
}
