/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.roomedit.mechanicedit.*;
import kr.syeyoung.dungeonsguide.roomedit.mechanicedit.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueEditRegistry {
    private static final Map<String, ValueEditCreator> valueEditMap = new HashMap<String, ValueEditCreator>();

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
        valueEditMap.put(Float.class.getName(), new ValueEditFloat.Generator());
        valueEditMap.put(OffsetPoint.class.getName(), new ValueEditOffsetPoint.Generator());
        valueEditMap.put(OffsetPointSet.class.getName(), new ValueEditOffsetPointSet.Generator());
        valueEditMap.put(Color.class.getName(), new ValueEditColor.Generator());
        valueEditMap.put(AColor.class.getName(), new ValueEditAColor.Generator());


        valueEditMap.put(DungeonSecret.class.getName(), new ValueEditSecret.Generator());
        valueEditMap.put(DungeonFairySoul.class.getName(), new ValueEditFairySoul.Generator());
        valueEditMap.put(DungeonNPC.class.getName(), new ValueEditNPC.Generator());
        valueEditMap.put(DungeonTomb.class.getName(), new ValueEditTomb.Generator());
        valueEditMap.put(DungeonBreakableWall.class.getName(), new ValueEditBreakableWall.Generator());
        valueEditMap.put(DungeonJournal.class.getName(), new ValueEditJournal.Generator());
        valueEditMap.put(DungeonDummy.class.getName(), new ValueEditDummy.Generator());

        valueEditMap.put(DungeonPressurePlate.class.getName(), new ValueEditPressurePlate.Generator());
        valueEditMap.put(DungeonOnewayLever.class.getName(), new ValueEditOnewayLever.Generator());
        valueEditMap.put(DungeonLever.class.getName(), new ValueEditLever.Generator());
        valueEditMap.put(DungeonDoor.class.getName(), new ValueEditDoor.Generator());
        valueEditMap.put(DungeonOnewayDoor.class.getName(), new ValueEditOnewayDoor.Generator());
    }
}
