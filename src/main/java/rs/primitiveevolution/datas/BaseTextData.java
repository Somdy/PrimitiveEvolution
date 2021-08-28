package rs.primitiveevolution.datas;

import org.dom4j.Element;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.utils.LMGameGeneralUtils;
import rs.primitiveevolution.Nature;

import java.util.HashMap;
import java.util.Map;

public class BaseTextData implements LMGameGeneralUtils {
    public final String ID;
    private final Map<Integer, SingleData> dataMap;
    
    public BaseTextData(String ID) {
        this.ID = ID;
        dataMap = new HashMap<>();
    }
    
    public SingleData getData(int branchID) {
        if (dataMap.containsKey(branchID)) 
            return dataMap.get(branchID);
        Nature.Log("Failed to find " + branchID + " evolved texts, check if it's missing");
        return new SingleData(branchID);
    }
    
    public final BaseTextData copyData(@NotNull Element data) {
        if (!data.getName().equals("BaseTextData") || !data.attributeValue("ID").equals(ID)) {
            Nature.Log(data.getName() + " is not a valid evolved card data relative with ID = " + ID);
            throw new IllegalArgumentException();
        }
        voidrun(() -> {
            for (Element e : data.elements()) {
                if (e.getName().equals("SData") && e.attribute("branch") != null) {
                    SingleData SData = new SingleData(Integer.parseInt(e.attributeValue("branch")));
                    if (e.elements("name") != null) {
                        SData.name = e.elementText("name");
                    }
                    if (e.elements("description") != null) {
                        SData.text = e.elementText("description");
                    }
                    dataMap.put(SData.branchID, SData);
                }
            }
            return null;
        });
        return this;
    }
    
    public final BaseTextData append(@NotNull Element data) {
        if (!data.getName().equals("BaseTextData") || !data.attributeValue("ID").equals(ID)) {
            Nature.Log(data.getName() + " is not a suitable evolved card data to append with whose ID = " + ID);
            throw new IllegalArgumentException();
        }
        voidrun(() -> {
            for (Element e : data.elements()) {
                if (e.getName().equals("SData") && e.attribute("branch") != null) {
                    SingleData SData = new SingleData(Integer.parseInt(e.attributeValue("branch")));
                    if (e.elements("name") != null) {
                        SData.name = e.elementText("name");
                    }
                    if (e.elements("description") != null) {
                        SData.text = e.elementText("description");
                    }
                    if (SData.valid())
                        dataMap.put(SData.branchID, SData);
                }
            }
            return null;
        });
        return this;
    }
    
    public class SingleData {
        public final int branchID;
        private String text;
        private String name;
        
        public SingleData(int branchID) {
            this.branchID = branchID;
            this.text = "missing description";
            this.name = "missing name";
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }
        
        public boolean valid() {
            return text != null || name != null;
        }
    }
}