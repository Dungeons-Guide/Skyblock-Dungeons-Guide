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

import java.awt.*;

public class TCRectangle implements FeatureTypeHandler<Rectangle> {
    public static final TCRectangle INSTANCE = new TCRectangle();

    @Override
    public Rectangle deserialize(JsonElement element) {
        Rectangle rectangle = new Rectangle();
        rectangle.x = ((JsonObject)element).get("x").getAsInt();
        rectangle.y = ((JsonObject)element).get("y").getAsInt();
        rectangle.width = ((JsonObject)element).get("width").getAsInt();
        rectangle.height = ((JsonObject)element).get("height").getAsInt();
        return rectangle;
    }

    @Override
    public JsonElement serialize(Rectangle element) {
        JsonObject object = new JsonObject();
        object.addProperty("x", element.x);
        object.addProperty("y", element.y);
        object.addProperty("width", element.width);
        object.addProperty("height", element.height);
        return object;
    }
}
