/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TCAColor implements TypeConverter<AColor> {
    @Override
    public String getTypeString() {
        return "acolor";
    }

    @Override
    public AColor deserialize(JsonElement element) {
        if (element instanceof JsonPrimitive)
            return new AColor(element.getAsInt(), true);

        JsonObject object = element.getAsJsonObject();
        AColor color = new AColor(object.get("color").getAsInt(), true);
        color.setChroma(object.get("chroma").getAsBoolean());
        color.setChromaSpeed(object.get("chromaSpeed").getAsFloat());
        return color;
    }

    @Override
    public JsonElement serialize(AColor element) {
        JsonObject object = new JsonObject();
        object.addProperty("color", element.getRGB());
        object.addProperty("chroma", element.isChroma());
        object.addProperty("chromaSpeed", element.getChromaSpeed());
        return object;
    }
}
