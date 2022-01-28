package rs.primitiveevolution.utils;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.helpers.ImageMaster;

public class EvoImageMst {
    public static Texture Lightning;
    public static Texture Frost_Right;
    public static Texture Frost_Mid;
    public static Texture Frost_Left;
    public static Texture[] Frost_VFX;
    public static Texture Dark;
    public static Texture Dark_Evoke;
    public static Texture Badge;
    
    public static void Initialize() {
        Lightning = ImageMaster.loadImage("PEAssets/images/orbs/lightning_overloaded.png");
        Frost_Right = ImageMaster.loadImage("PEAssets/images/orbs/frostRight.png");
        Frost_Mid = ImageMaster.loadImage("PEAssets/images/orbs/frostMid.png");
        Frost_Left = ImageMaster.loadImage("PEAssets/images/orbs/frostLeft.png");
        Frost_VFX = new Texture[3];
        Frost_VFX[0] = ImageMaster.loadImage("PEAssets/images/orbs/f1.png");
        Frost_VFX[1] = ImageMaster.loadImage("PEAssets/images/orbs/f2.png");
        Frost_VFX[2] = ImageMaster.loadImage("PEAssets/images/orbs/f3.png");
        Dark = ImageMaster.loadImage("PEAssets/images/orbs/dark.png");
        Dark_Evoke = ImageMaster.loadImage("PEAssets/images/orbs/darkEvoke2.png");
        Badge = ImageMaster.loadImage("PEAssets/images/badge.png");
    }
}