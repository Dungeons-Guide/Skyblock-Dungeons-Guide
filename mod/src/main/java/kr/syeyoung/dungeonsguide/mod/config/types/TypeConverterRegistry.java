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

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/config/types/TypeConverterRegistry.java
package kr.syeyoung.dungeonsguide.config.types;
========
package kr.syeyoung.dungeonsguide.mod.config.types;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/config/types/TypeConverterRegistry.java

import java.util.HashMap;
import java.util.Map;

public class TypeConverterRegistry {
    private static final Map<String, TypeConverter> typeConverterMap = new HashMap<String, TypeConverter>();

    public static void register(TypeConverter typeConverter) {
        typeConverterMap.put(typeConverter.getTypeString(), typeConverter);
    }

    public static TypeConverter getTypeConverter(String type_string) {
        return typeConverterMap.get(type_string);
    }
    public static <T> TypeConverter<T> getTypeConverter(String type_string, Class<T> t) {
        return (TypeConverter<T>)typeConverterMap.get(type_string);
    }

    static {
        register(new TCBoolean());
        register(new TCInteger());
        register(new TCRectangle());
        register(new TCGUIRectangle());
        register(new TCString());
        register(new TCColor());
        register(new TCFloat());
        register(new TCAColor());
        register(new TCTextStyleList());
        register(new TCTextStyle());
        register(new TCStringList());
        register(new TCKeybind());
    }
}
