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



public class TCTextStyle implements TypeConverter<TextStyle> {
    @Override
    public String getTypeString() {
        return "textstyle";
    }

    @Override
    public TextStyle deserialize(JsonElement element) {
        TextStyle textStyle = new TextStyle();
        textStyle.setColor(TypeConverterRegistry.getTypeConverter("acolor", AColor.class).deserialize(element.getAsJsonObject().get("color")));
        textStyle.setBackground(element.getAsJsonObject().has("background") ? TypeConverterRegistry.getTypeConverter("acolor", AColor.class).deserialize(element.getAsJsonObject().get("background"))
                : new AColor(0x00777777, true));
        textStyle.setGroupName(element.getAsJsonObject().get("group").getAsString());
        if (element.getAsJsonObject().has("shadow"))
        textStyle.setShadow(element.getAsJsonObject().get("shadow").getAsBoolean());
        return textStyle;
    }

    @Override
    public JsonElement serialize(TextStyle element) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("color", TypeConverterRegistry.getTypeConverter("acolor", AColor.class).serialize(element.getColor()));
        jsonObject.add("background", TypeConverterRegistry.getTypeConverter("acolor", AColor.class).serialize(element.getBackground()));
        jsonObject.addProperty("group", element.getGroupName());
        jsonObject.addProperty("shadow", element.isShadow());
        return jsonObject;
    }
}
