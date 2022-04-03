package rs.primitiveevolution.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import org.jetbrains.annotations.NotNull;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.interfaces.EvolvableCard;

public class EvolveIconPatches {
    public static final Texture ICON = ImageMaster.loadImage("PEAssets/images/badge.png");
    
    @SpirePatch(clz = AbstractCard.class, method = "renderCard")
    public static class RenderIconOnCardPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractCard _inst, SpriteBatch sb, boolean h, boolean s) {
            if (_inst instanceof EvolvableCard && Nature.EVOLVE_ICON_ON
                    && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                renderIcon(sb, _inst.current_x + _inst.hb.width * Settings.scale,
                        _inst.current_y + (_inst.hb.height / 2F - 80F) * Settings.scale, _inst.drawScale);
            }
        }
        private static void renderIcon(@NotNull SpriteBatch sb, float x, float y, float scale) {
            sb.setColor(Color.WHITE.cpy());
            sb.draw(ICON, x, y, 31F / 2F, 32F / 2F, 31F, 32F, 
                    scale * Settings.scale, scale * Settings.scale, 0, 
                    0, 0, 31, 32, false, false);
        }
    }
    
    @SpirePatch(clz = AbstractCard.class, method = "renderInLibrary")
    public static class RenderIconInLibraryPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractCard _inst, SpriteBatch sb) {
            if (_inst instanceof EvolvableCard && Nature.EVOLVE_ICON_ON
                    && ((EvolvableCard) _inst).canBranch() && !_inst.upgraded) {
                renderIcon(sb, _inst.current_x + _inst.hb.width * Settings.scale,
                        _inst.current_y + (_inst.hb.height / 2F - 80F) * Settings.scale, _inst.drawScale);
            }
        }
        private static void renderIcon(@NotNull SpriteBatch sb, float x, float y, float scale) {
            sb.setColor(Color.WHITE.cpy());
            sb.draw(ICON, x, y, 31F / 2F, 32F / 2F, 31F, 32F,
                    scale * Settings.scale, scale * Settings.scale, 0,
                    0, 0, 31, 32, false, false);
        }
    }
}