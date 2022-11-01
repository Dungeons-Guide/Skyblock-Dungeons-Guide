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

package kr.syeyoung.dungeonsguide.mod.utils;

import net.minecraft.client.renderer.GlStateManager;

import java.lang.reflect.Field;
import java.util.*;

public class GlStateUtils {
    public static Map<String, Object> dumpStates() {
        Map<String, Object> primitiveDump = new LinkedHashMap<>();
        try {
            recursivelyDump(primitiveDump, "GlStateManager", null, GlStateManager.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            primitiveDump.put("$ERROR", true);
        }
        return primitiveDump;
    }

    public static void printDump(Map<String, Object> dump) {
        for (Map.Entry<String, Object> stringObjectEntry : dump.entrySet()) {
            System.out.println(stringObjectEntry+": "+stringObjectEntry.getValue());
        }
    }

    public static void compareDump(Map<String, Object> dump1, Map<String,Object> dump2) {
        Set<String> set = new HashSet<>();
        set.addAll(dump1.keySet());
        set.addAll(dump2.keySet());

        for (String s : set) {
            Object obj1 = dump1.get(s);
            Object obj2 = dump2.get(s);
            if (!Objects.equals(obj1, obj2)) System.out.println(s+": Prev {"+obj1+"} New {"+obj2+"}");
        }
    }

    public static void recursivelyDump(Map<String, Object> primitiveDump, String objPath, Object obj, Class clazz) throws IllegalAccessException {
        primitiveDump.put(objPath+".$class", clazz.getName());
        for (Field declaredField : clazz.getDeclaredFields()) {
            declaredField.setAccessible(true);
            Object fieldData = declaredField.get(obj);
            if (fieldData.getClass().getName().startsWith("java.lang")) {
                primitiveDump.put(objPath+"."+declaredField.getName(), fieldData);
            } else {
                recursivelyDump(primitiveDump, objPath+"."+declaredField.getName(), fieldData, fieldData.getClass());
            }
        }
    }
}
