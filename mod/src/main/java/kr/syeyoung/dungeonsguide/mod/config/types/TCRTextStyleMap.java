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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultingDelegatingTextStyle;

import java.util.HashMap;
import java.util.Map;


public class TCRTextStyleMap implements FeatureTypeHandler<Map<String, DefaultingDelegatingTextStyle>> {
    public static final TCRTextStyleMap INSTANCE = new TCRTextStyleMap();
    @Override
    public Map<String, DefaultingDelegatingTextStyle> deserialize(JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();
        Map<String, DefaultingDelegatingTextStyle> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> stringJsonElementEntry : jsonObject.entrySet()) {
            map.put(stringJsonElementEntry.getKey(), TCRTextStyle.INSTANCE.deserialize(stringJsonElementEntry.getValue()));
        }
        return map;
    }

    @Override
    public JsonElement serialize(Map<String, DefaultingDelegatingTextStyle> element) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, DefaultingDelegatingTextStyle> stringDefaultingDelegatingTextStyleEntry : element.entrySet()) {
            jsonObject.add(stringDefaultingDelegatingTextStyleEntry.getKey(), TCRTextStyle.INSTANCE.serialize(stringDefaultingDelegatingTextStyleEntry.getValue()));
        }
        return jsonObject;
    }
}
