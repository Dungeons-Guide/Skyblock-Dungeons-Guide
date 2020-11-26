package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueEditRegistry {
    private static Map<String, ValueEditCreator> valueEditMap = new HashMap<String, ValueEditCreator>();

    public static ValueEditCreator getValueEditMap(String className) {
        return valueEditMap.get(className);
    }

    public static List<String> getClassesSupported() {
        return new ArrayList<String>(valueEditMap.keySet());
    }

    static {
        valueEditMap.put("null", new ValueEditNull());
        valueEditMap.put(String.class.getName(), new ValueEditString.Generator());
        valueEditMap.put(Boolean.class.getName(), new ValueEditBoolean.Generator());
        valueEditMap.put(Integer.class.getName(), new ValueEditInteger.Generator());
        valueEditMap.put(OffsetPoint.class.getName(), new ValueEditOffsetPoint.Generator());
    }
}
