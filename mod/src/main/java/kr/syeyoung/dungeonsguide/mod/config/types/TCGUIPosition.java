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

public class TCGUIPosition implements TypeConverter<GUIPosition> {
    @Override
    public String getTypeString() {
        return "guipos";
    }

    @Override
    public GUIPosition deserialize(JsonElement element) {
        if (element == null) return null;
        GUIPosition rectangle = new GUIPosition();
        rectangle.setXOffset(((JsonObject)element).get("x").getAsDouble());
        rectangle.setYOffset(((JsonObject)element).get("y").getAsDouble());
        rectangle.setXType(GUIPosition.OffsetType.values()[((JsonObject)element).get("xType").getAsInt()]);
        rectangle.setYType(GUIPosition.OffsetType.values()[((JsonObject)element).get("yType").getAsInt()]);
        rectangle.setWidth(element.getAsJsonObject().get("width") == null ? null : element.getAsJsonObject().get("width").getAsDouble());
        rectangle.setHeight(element.getAsJsonObject().get("height") == null ? null : element.getAsJsonObject().get("height").getAsDouble());
        return rectangle;
    }

    @Override
    public JsonElement serialize(GUIPosition element) {
        JsonObject object = new JsonObject();
        object.addProperty("x", element.getXOffset());
        object.addProperty("y", element.getYOffset());
        object.addProperty("xType", element.getXType().ordinal());
        object.addProperty("yType", element.getYType().ordinal());
        object.addProperty("width", element.getWidth());
        object.addProperty("height", element.getHeight());
        return object;
    }
}
