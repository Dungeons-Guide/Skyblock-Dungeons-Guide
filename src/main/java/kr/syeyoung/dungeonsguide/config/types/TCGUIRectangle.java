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

package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;

public class TCGUIRectangle implements TypeConverter<GUIRectangle> {
    @Override
    public String getTypeString() {
        return "guirect";
    }

    @Override
    public GUIRectangle deserialize(JsonElement element) {
        if (element == null) return null;
        GUIRectangle rectangle = new GUIRectangle();
        rectangle.setX(((JsonObject)element).get("x").getAsInt());
        rectangle.setY(((JsonObject)element).get("y").getAsInt());
        rectangle.setWidth(((JsonObject)element).get("width").getAsInt());
        rectangle.setHeight(((JsonObject)element).get("height").getAsInt());
        return rectangle;
    }

    @Override
    public JsonElement serialize(GUIRectangle element) {
        JsonObject object = new JsonObject();
        object.addProperty("x", element.getX());
        object.addProperty("y", element.getY());
        object.addProperty("width", element.getWidth());
        object.addProperty("height", element.getHeight());
        return object;
    }
}
