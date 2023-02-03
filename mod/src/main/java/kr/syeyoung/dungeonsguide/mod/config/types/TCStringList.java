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

package kr.syeyoung.dungeonsguide.mod.config.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

public class TCStringList implements FeatureTypeHandler<List<String>> {
    public static final TCStringList INSTANCE = new TCStringList();

    @Override
    public List<String> deserialize(JsonElement element) {
        List<String> strList = new ArrayList<>();
        for (JsonElement jsonElement : element.getAsJsonArray()) {
            strList.add(jsonElement.getAsString());
        }
        return strList;
    }

    @Override
    public JsonElement serialize(List<String> element) {
        JsonArray jsonElements = new JsonArray();
        for (String s : element) {
            jsonElements.add(new JsonPrimitive(s));
        }
        return jsonElements;
    }
}
