package rs.primitiveevolution.datas;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.core.Settings;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.primitiveevolution.Nature;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DataPool {
    public static BaseTextData IRONCLADS;
    public static BaseTextData SILENTS;
    public static BaseTextData DEFECTS;
    public static BaseTextData WATCHERS;
    public static BaseTextData COLORLESS;
    
    private static final String[] FILES = {"attacks", "powers", "skills"};
    
    private static SAXReader reader;
    private static String lang;
    
    public static void Initialize() {
        long time = System.currentTimeMillis();
        reader = new SAXReader();
        lang = supportedLang();
        loadSilentDatas();
        loadIroncladDatas();
        loadDefectDatas();
        loadWatcherDatas();
        loadColorlessDatas();
        Nature.Log("ALL data is loaded: " + (System.currentTimeMillis() - time) + " ms");
    }
    
    @NotNull
    @Contract(pure = true)
    private static String supportedLang() {
        switch (Settings.language) {
            case ZHS:
                return "zhs";
            case ZHT:
                return "zht";
            default:
                return "eng";
        }
    }
    
    private static void loadSilentDatas() {
        try {
            Element[] elements = LoadElementsFromPath("PEAssets/locals/" + lang + "/silent", FILES);
            Element data;
            for (int i = 0; i < elements.length; i++) {
                if (i == 0) {
                    Element head = elements[i];
                    data = head.element("BaseTextData");
                    SILENTS = new BaseTextData("Silent").copyData(data);
                    continue;
                }
                Element e = elements[i];
                data = e.element("BaseTextData");
                SILENTS.append(data);
            }
        } catch (Exception e) {
            Nature.Log("Failed to load silent's card datas");
            e.printStackTrace();
        }
    }

    private static void loadIroncladDatas() {
        try {
            Element[] elements = LoadElementsFromPath("PEAssets/locals/" + lang + "/ironclad", FILES);
            Element data;
            for (int i = 0; i < elements.length; i++) {
                if (i == 0) {
                    Element head = elements[i];
                    data = head.element("BaseTextData");
                    IRONCLADS = new BaseTextData("Ironclad").copyData(data);
                    continue;
                }
                Element e = elements[i];
                data = e.element("BaseTextData");
                IRONCLADS.append(data);
            }
        } catch (Exception e) {
            Nature.Log("Failed to load ironclad's card datas");
            e.printStackTrace();
        }
    }

    private static void loadDefectDatas() {
        try {
            Element[] elements = LoadElementsFromPath("PEAssets/locals/" + lang + "/defect", FILES);
            Element data;
            for (int i = 0; i < elements.length; i++) {
                if (i == 0) {
                    Element head = elements[i];
                    data = head.element("BaseTextData");
                    DEFECTS = new BaseTextData("Defect").copyData(data);
                    continue;
                }
                Element e = elements[i];
                data = e.element("BaseTextData");
                DEFECTS.append(data);
            }
        } catch (Exception e) {
            Nature.Log("Failed to load defect's card datas");
            e.printStackTrace();
        }
    }

    private static void loadWatcherDatas() {
        try {
            Element[] elements = LoadElementsFromPath("PEAssets/locals/" + lang + "/watcher", FILES);
            Element data;
            for (int i = 0; i < elements.length; i++) {
                if (i == 0) {
                    Element head = elements[i];
                    data = head.element("BaseTextData");
                    WATCHERS = new BaseTextData("Watcher").copyData(data);
                    continue;
                }
                Element e = elements[i];
                data = e.element("BaseTextData");
                WATCHERS.append(data);
            }
        } catch (Exception e) {
            Nature.Log("Failed to load watcher's card datas");
            e.printStackTrace();
        }
    }
    
    private static void loadColorlessDatas() {
        try {
            Element[] elements = LoadElementsFromPath("PEAssets/locals/" + lang + "/colorless", FILES);
            Element data;
            for (int i = 0; i < elements.length; i++) {
                if (i == 0) {
                    Element head = elements[i];
                    data = head.element("BaseTextData");
                    COLORLESS = new BaseTextData("Colorless").copyData(data);
                    continue;
                }
                Element e = elements[i];
                data = e.element("BaseTextData");
                COLORLESS.append(data);
            }
        } catch (Exception e) {
            Nature.Log("Failed to load colorless card datas");
            e.printStackTrace();
        }
    }

    @NotNull
    private static Element[] LoadElementsFromPath(String dirPath, @NotNull String... fileNames) throws Exception {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < fileNames.length; i++) {
            String path = dirPath + (dirPath.endsWith("/") ? "" : "/") + (fileNames[i].endsWith(".xml")
                    ? fileNames[i].replace(".xml", "") : fileNames[i]);
            if (Gdx.files.internal(path).exists()) {
                Nature.Log("Target data file located: " + path);
                paths.add(path);
            }
        }
        URL[] urls = new URL[paths.size()];
        Element[] elements = new Element[urls.length];
        for (int i = 0; i < paths.size(); i++) {
            urls[i] = LoadFileFromString(paths.get(i));
        }
        for (int i = 0; i < urls.length; i++) {
            elements[i] = RootElement(urls[i]);
        }
        return elements;
    }

    private static Element RootElement(URL path) throws DocumentException {
        return reader.read(path).getRootElement();
    }
    
    private static URL LoadFileFromString(String path) {
        return DataPool.class.getClassLoader().getResource(path);
    }
}