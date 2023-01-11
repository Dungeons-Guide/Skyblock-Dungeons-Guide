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

import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.Parser;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.ParserException;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.W3CBackedParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class DomElementRegistry {
    private static final Map<String, ParsedWidgetConverter> converters = new HashMap<>();

    public static <T extends Widget, R extends Widget & ImportingWidget> ParsedWidgetConverter<T, R> obtainConverter(String name) {
        return converters.get(name);
    }

    public static Parser obtainParser(ResourceLocation resourceLocation) {
        try {
            IResource iResource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
            return new W3CBackedParser(iResource.getInputStream());
        } catch (Exception e) {
            throw new ParserException("An error occured while parsing "+resourceLocation, e);
        }
    }
}
