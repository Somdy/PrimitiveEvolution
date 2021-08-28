package rs.primitiveevolution;

import basemod.BaseMod;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.LMDebug;
import rs.lazymankits.utils.LMGameGeneralUtils;
import rs.lazymankits.utils.LMKeyword;
import rs.primitiveevolution.cards.Evolution;
import rs.primitiveevolution.datas.DataPool;
import rs.primitiveevolution.utils.EvoImageMst;

import java.util.HashMap;
import java.util.Map;

@SpireInitializer
public class Nature implements LMGameGeneralUtils, PostInitializeSubscriber, EditStringsSubscriber, EditKeywordsSubscriber {
    public static final String MODID = "prevolution";
    
    public static void initialize() {
        new Nature();
    }
    
    public Nature() {
        BaseMod.subscribe(this);
    }
    
    @NotNull
    @Contract(pure = true)
    public static String MakeID(String origin) {
        return MODID + ":" + origin;
    }
    
    public static void Log(Object what) {
        LMDebug.Log(Nature.class, "======evo======" + what + "======evo======");
    }

    @Override
    public void receivePostInitialize() {
        DataPool.Initialize();
        Evolution.Initialize();
        EvoImageMst.Initialize();
    }

    @Override
    public void receiveEditStrings() {
        String lang = getSupportedLanguage(Settings.language);
        BaseMod.loadCustomStringsFile(PowerStrings.class, "PEAssets/locals/" + lang + "/powers.json");
        BaseMod.loadCustomStringsFile(UIStrings.class, "PEAssets/locals/" + lang + "/ui.json");
    }

    @Override
    public void receiveEditKeywords() {
        String lang = getSupportedLanguage(Settings.language);
        Map<String, LMKeyword> keywordMap = LMKeyword.SelfFromJson("PEAssets/locals/" + lang + "/keywords.json");
        keywordMap.forEach((k, v) -> BaseMod.addKeyword(MODID + ":", v.PROPER, v.NAMES, v.DESCRIPTION));
        loadLocalKeywords();
    }
    
    private void loadLocalKeywords() {
        Map<String, String> map = new HashMap<>();
        switch (Settings.language) {
            case ZHS:
                map.put(Evolution.GetEvoKeyword(), "该牌有多种不同的升级分支");
                break;
            default:
                map.put(Evolution.GetEvoKeyword(), "This card has different upgrade branches.");
        }
        map.forEach((k, v) -> BaseMod.addKeyword(new String[]{k}, v));
    }
}