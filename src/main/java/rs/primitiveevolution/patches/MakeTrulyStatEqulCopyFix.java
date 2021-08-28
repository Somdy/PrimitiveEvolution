package rs.primitiveevolution.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

@SpirePatch(clz = AbstractCard.class, method = "makeStatEquivalentCopy")
public class MakeTrulyStatEqulCopyFix {
    @SpireInsertPatch( rloc = 21, localvars = {"card"})
    public static void Insert(AbstractCard _inst, AbstractCard card) {
        card.purgeOnUse = _inst.purgeOnUse;
        card.isEthereal = _inst.isEthereal;
        card.exhaust = _inst.exhaust;
    }
}