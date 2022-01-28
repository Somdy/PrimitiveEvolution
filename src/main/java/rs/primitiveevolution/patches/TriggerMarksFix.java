package rs.primitiveevolution.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.purple.PressurePoints;
import com.megacrit.cardcrawl.powers.watcher.MarkPower;
import rs.lazymankits.utils.LMSK;

public class TriggerMarksFix {
    @SpirePatch(clz = MarkPower.class, method = "triggerMarks")
    public static class TriggerMarksPatch {
        @SpirePrefixPatch
        public static void Prefix(MarkPower _inst, AbstractCard card) {
            if (!card.cardID.equals(PressurePoints.ID) && !(card instanceof PressurePoints)) {
                LMSK.AddToBot(new LoseHPAction(_inst.owner, null, _inst.amount, AbstractGameAction.AttackEffect.FIRE));
            }
        }
    }
}