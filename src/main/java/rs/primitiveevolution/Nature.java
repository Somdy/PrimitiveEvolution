package rs.primitiveevolution;

import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
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

import java.util.*;

@SpireInitializer
public class Nature implements LMGameGeneralUtils, PostInitializeSubscriber, EditStringsSubscriber, EditKeywordsSubscriber, 
        PostUpdateSubscriber {
    public static final String MODID = "prevolution";
    public static final String MODNAME = "Primitive Evolution";
    public static final String[] AUTHORS = {"Somdy", "Carcinogen"};
    public static final String DESCRIPTION = "提供部分原版卡牌不同的升级路线。";
    public static boolean ALLOW_BRANCHES;
    public static boolean EVOLVE_ICON_ON;
    
    private static List<AbstractGameAction> actions = new ArrayList<>();
    
    public static void initialize() {
        new Nature();
    }
    
    public Nature() {
        BaseMod.subscribe(this);
        ALLOW_BRANCHES = true;
        EVOLVE_ICON_ON = true;
        SpireConfig config = MakeConfig();
        LoadConfig(config);
    }
    
    public static SpireConfig MakeConfig() {
        Properties defaults = new Properties();
        defaults.setProperty("ALLOW_BRANCHES", Boolean.toString(true));
        defaults.setProperty("EVOLVE_ICON_ON", Boolean.toString(true));
        try {
            SpireConfig config = new SpireConfig("PrimitiveEvolution", "PEConfig", defaults);
            return config;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static void LoadConfig(SpireConfig config) {
        if (config != null) {
            ALLOW_BRANCHES = config.getBool("ALLOW_BRANCHES");
            EVOLVE_ICON_ON = config.getBool("EVOLVE_ICON_ON");
        }
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
        
        makeModPanels();
    }
    
    private static void makeModPanels() {
        ModPanel settings = new ModPanel();
        ModLabeledToggleButton ALLOW_BRANCHES_BTN = new ModLabeledToggleButton("启用分支升级(Enable upgrade branches)",
                350F, 700F, Color.WHITE.cpy(), FontHelper.charDescFont, ALLOW_BRANCHES, settings, (l) -> {},
                (btn) -> {
            ALLOW_BRANCHES = btn.enabled;
            try {
                SpireConfig config = MakeConfig();
                assert config != null;
                config.setBool("ALLOW_BRANCHES", ALLOW_BRANCHES);
                config.save();
            } catch (Exception e) {
                Log("Failed to initialize PE panel");
                e.printStackTrace();
            }
                });
        ModLabeledToggleButton EVOLVE_ICON_BTN = new ModLabeledToggleButton("启用可进化图标(Enable evolution icon)",
                350F, 660F, Color.WHITE.cpy(), FontHelper.charDescFont, EVOLVE_ICON_ON, settings, (l) -> {},
                (btn) -> {
            EVOLVE_ICON_ON = btn.enabled;
                    try {
                        SpireConfig config = MakeConfig();
                        assert config != null;
                        config.setBool("EVOLVE_ICON_ON", EVOLVE_ICON_ON);
                        config.save();
                    } catch (Exception e) {
                        Log("Failed to initialize PE panel");
                        e.printStackTrace();
                    }
                });
        settings.addUIElement(ALLOW_BRANCHES_BTN);
        settings.addUIElement(EVOLVE_ICON_BTN);
        BaseMod.registerModBadge(EvoImageMst.Badge, MODNAME, Arrays.toString(AUTHORS), DESCRIPTION, settings);
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
            case ZHT:
                map.put(Evolution.GetEvoKeyword(), "該牌有多種不同的陞級分支");
                break;
            default:
                map.put(Evolution.GetEvoKeyword(), "This card has different upgrade branches.");
        }
        map.forEach((k, v) -> BaseMod.addKeyword(new String[]{k}, v));
    }
    
    public static void AddToBot(AbstractGameAction action) {
        actions.add(action);
    }
    
    public static void AddToTop(AbstractGameAction action) {
        actions.add(0, action);
    }
    
    @Override
    public void receivePostUpdate() {
        if (actions.size() > 0) {
            if (!AbstractDungeon.isScreenUp) {
                actions.get(0).update();
            }
            if (actions.get(0).isDone) {
                Log("Removing finished action: " + actions.get(0).getClass().getSimpleName());
                actions.remove(0);
            }
        }
    }
}