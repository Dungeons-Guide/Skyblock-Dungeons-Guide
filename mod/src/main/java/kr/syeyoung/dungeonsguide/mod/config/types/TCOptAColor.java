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
import com.google.gson.JsonPrimitive;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.config.types.coloredit.ColorEditPopup;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.AbsLocationPopup;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class TCOptAColor implements FeatureTypeHandler<Optional<AColor>> {
    public static final TCOptAColor INSTANCE = new TCOptAColor();
    @Override
    public Optional<AColor> deserialize(JsonElement element) {
        if (element instanceof JsonPrimitive)
            return Optional.of(new AColor(element.getAsInt(), true));

        JsonObject object = element.getAsJsonObject();
        if (object.has("present") && !object.get("present").isJsonNull() && object.get("present").getAsBoolean()) {
            AColor color = new AColor(object.get("color").getAsInt(), true);
            color.setChroma(object.get("chroma").getAsBoolean());
            color.setChromaSpeed(object.get("chromaSpeed").getAsFloat());
            return Optional.of(color);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public JsonElement serialize(Optional<AColor> element) {
        JsonObject object = new JsonObject();
        if (element.isPresent()) {
            object.addProperty("present", true);
            object.addProperty("color", element.get().getRGB());
            object.addProperty("chroma", element.get().isChroma());
            object.addProperty("chromaSpeed", element.get().getChromaSpeed());
        } else {
            object.addProperty("present", false);
        }
        return object;
    }

}
