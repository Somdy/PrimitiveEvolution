package rs.primitiveevolution.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.interfaces.EvolvableCard;

@SpirePatch(clz = AbstractCard.class, method = "initializeDescription")
public class AddEvolvableTooltipPatch {
    @SpireInsertPatch(rloc = 1)
    public static void Insert(AbstractCard _inst) {
        if (_inst instanceof EvolvableCard && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
            _inst.keywords.add(Evolution.GetEvoKeyword());
        }
    }
}