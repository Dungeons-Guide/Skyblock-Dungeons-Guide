package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.roomedit.mechanicedit.*;

import java.awt.*;
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
        valueEditMap.put(OffsetPointSet.class.getName(), new ValueEditOffsetPointSet.Generator());
        valueEditMap.put(Color.class.getName(), new ValueEditColor.Generator());
        valueEditMap.put(AColor.class.getName(), new ValueEditAColor.Generator());


        valueEditMap.put(DungeonSecret.class.getName(), new ValueEditSecret.Generator());
        valueEditMap.put(DungeonTomb.class.getName(), new ValueEditTomb.Generator());
        valueEditMap.put(DungeonBreakableWall.class.getName(), new ValueEditBreakableWall.Generator());

        valueEditMap.put(DungeonPressurePlate.class.getName(), new ValueEditPressurePlate.Generator());
        valueEditMap.put(DungeonOnewayLever.class.getName(), new ValueEditOnewayLever.Generator());
        valueEditMap.put(DungeonLever.class.getName(), new ValueEditLever.Generator());
        valueEditMap.put(DungeonDoor.class.getName(), new ValueEditDoor.Generator());
        valueEditMap.put(DungeonOnewayDoor.class.getName(), new ValueEditOnewayDoor.Generator());
    }
}
