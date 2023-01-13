/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2.xml;

import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;

public final class StringConversions {
    public static <T> T convert(Class<T> clazz, String val) {
        if (clazz== Float.class) {
            return (T) Float.valueOf(val);
        } else if (clazz== Double.class) {
            return (T) Double.valueOf(val);
        } else if (clazz== Integer.class) {
            if (val.startsWith("#"))
                return (T) Integer.valueOf(Integer.parseUnsignedInt(val.substring(1), 16));
            if (val.startsWith("0x"))
                return (T) Integer.valueOf(Integer.parseUnsignedInt(val.substring(2), 16));
            return (T) Integer.valueOf(val);
        } else if (clazz== Short.class) {
            if (val.startsWith("0x"))
                return (T) Short.valueOf((short) Integer.parseUnsignedInt(val.substring(2), 16));
            return (T) Short.valueOf(val);
        }  else if (clazz== String.class) {
            return (T) val;
        } else if (clazz.isEnum()) {
            for (Object enumConstant : clazz.getEnumConstants()) {
                if (val.equalsIgnoreCase(enumConstant.toString()))
                    return (T) enumConstant;
            }
        } else if (clazz== Boolean.class) {
            return (T) Boolean.valueOf(val);
        }
        throw new UnsupportedOperationException("cant convert to "+clazz.getName());
    }
}
