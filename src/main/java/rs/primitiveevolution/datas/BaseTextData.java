package rs.primitiveevolution.datas;

import org.dom4j.Element;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.utils.LMGameGeneralUtils;
import rs.primitiveevolution.Nature;

import java.util.HashMap;
import java.util.List;
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
                    if (e.element("msg") != null) {
                        List<Element> msg = e.element("msg").elements();
                        SData.initMsg(msg.size());
                        for (int i = 0; i < msg.size(); i++) {
                            SData.setMsg(i, msg.get(i).getText());
                        }
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
                    if (e.element("msg") != null) {
                        List<Element> msg = e.element("msg").elements();
                        SData.initMsg(msg.size());
                        for (int i = 0; i < msg.size(); i++) {
                            SData.setMsg(i, msg.get(i).getText());
                        }
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
        private String[] msg;
        
        public SingleData(int branchID) {
            this.branchID = branchID;
            this.text = "missing description";
            this.name = "missing name";
            this.msg = new String[] {"missing_msg_0", "missing_msg_1"};
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }
        
        private SingleData initMsg(int size) {
            msg = new String[size];
            return this;
        }
        
        private SingleData setMsg(int slot, String what) {
            if (slot >= msg.length) {
                Nature.Log("Asked for " + slot + "th msg but " + name + " has only " + (msg.length - 1));
                slot = msg.length - 1;
            }
            msg[slot] = what;
            return this;
        }
        
        public String getMsg(int slot) {
            if (slot >= msg.length) {
                Nature.Log("Asked for " + slot + "th msg but " + name + " has only " + (msg.length - 1));
                slot = msg.length - 1;
            }
            return msg[slot];
        }
        
        public String[] getMsg() {
            return msg;
        }
        
        public boolean valid() {
            return text != null || name != null;
        }
    }
}