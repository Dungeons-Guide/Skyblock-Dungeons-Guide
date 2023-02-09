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
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;


public class TCRTextStyle implements FeatureTypeHandler<DefaultingDelegatingTextStyle> {
    public static final TCRTextStyle INSTANCE = new TCRTextStyle();

    @Override
    public DefaultingDelegatingTextStyle deserialize(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        DefaultingDelegatingTextStyle textStyle = new DefaultingDelegatingTextStyle();
        textStyle.setSize(object.has("size") && !object.get("size").isJsonNull() ? object.get("size").getAsDouble() : null);
        textStyle.setTopAscent(object.has("topAscent") && !object.get("topAscent").isJsonNull()? object.get("topAscent").getAsDouble() : null);
        textStyle.setBottomAscent(object.has("bottomAscent") && !object.get("bottomAscent").isJsonNull()? object.get("bottomAscent").getAsDouble() : null);
        textStyle.setBold(object.has("bold") && !object.get("bold").isJsonNull()? object.get("bold").getAsBoolean() : null);
        textStyle.setItalics(object.has("italics") && !object.get("italics").isJsonNull()? object.get("italics").getAsBoolean() : null);
        textStyle.setStrikeThrough(object.has("strikethrough") && !object.get("strikethrough").isJsonNull()? object.get("strikethrough").getAsBoolean() : null);
        textStyle.setUnderline(object.has("underline") && !object.get("underline").isJsonNull()? object.get("underline").getAsBoolean() : null);
        textStyle.setOutline(object.has("outline") && !object.get("outline").isJsonNull()? object.get("outline").getAsBoolean() : null);
        textStyle.setShadow(object.has("shadow") && !object.get("shadow").isJsonNull()? object.get("shadow").getAsBoolean() : null);
        textStyle.setBackgroundShader(object.has("backgroundColor") && !object.get("backgroundColor").isJsonNull()? TCAColor.INSTANCE.deserialize(object.get("backgroundColor")) : null);
        textStyle.setTextShader(object.has("textColor") && !object.get("textColor").isJsonNull()? TCAColor.INSTANCE.deserialize(object.get("textColor")) : null);
        textStyle.setStrikeThroughShader(object.has("strikethroughColor") && !object.get("strikethroughColor").isJsonNull()? TCOptAColor.INSTANCE.deserialize(object.get("strikethroughColor")) : null);
        textStyle.setUnderlineShader(object.has("underlineColor") && !object.get("underlineColor").isJsonNull()? TCOptAColor.INSTANCE.deserialize(object.get("underlineColor")) : null);
        textStyle.setOutlineShader(object.has("outlineColor") && !object.get("outlineColor").isJsonNull()? TCOptAColor.INSTANCE.deserialize(object.get("outlineColor")) : null);
        textStyle.setShadowShader(object.has("shadowColor") && !object.get("shadowColor").isJsonNull()? TCOptAColor.INSTANCE.deserialize(object.get("shadowColor")) : null);
        return textStyle;
    }

    @Override
    public JsonElement serialize(DefaultingDelegatingTextStyle element) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("size", element.size);
        jsonObject.addProperty("topAscent", element.topAscent);
        jsonObject.addProperty("bottomAscent", element.bottomAscent);
        jsonObject.addProperty("bold", element.bold);
        jsonObject.addProperty("italics", element.italics);
        jsonObject.addProperty("strikethrough", element.strikeThrough);
        jsonObject.addProperty("underline", element.underline);
        jsonObject.addProperty("outline", element.outline);
        jsonObject.addProperty("shadow", element.shadow);
        jsonObject.add("backgroundColor", element.backgroundShader == null ? null : TCAColor.INSTANCE.serialize(element.backgroundShader));
        jsonObject.add("textColor", element.textShader == null ? null : TCAColor.INSTANCE.serialize(element.textShader));
        jsonObject.add("strikethroughColor", element.strikeThroughShader == null ? null : TCOptAColor.INSTANCE.serialize(element.strikeThroughShader));
        jsonObject.add("underlineColor", element.underlineShader == null ? null : TCOptAColor.INSTANCE.serialize(element.underlineShader));
        jsonObject.add("outlineColor", element.outlineShader == null ? null : TCOptAColor.INSTANCE.serialize(element.outlineShader));
        jsonObject.add("shadowColor", element.shadowShader == null ? null : TCOptAColor.INSTANCE.serialize(element.shadowShader));
        return jsonObject;
    }
}
