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
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.view.TestView;
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

    static {
        converters.put("stack", new ExportedWidgetConverter(Stack::new));
        converters.put("size", new ExportedWidgetConverter(SizedBox::new));
        converters.put("scaler", new ExportedWidgetConverter(Scaler::new));
        converters.put("row", new ExportedWidgetConverter(Row::new));
        converters.put("padding", new ExportedWidgetConverter(Padding::new));
        converters.put("col", new ExportedWidgetConverter(Column::new));
        converters.put("bgcolor", new ExportedWidgetConverter(Background::new));
        converters.put("flexible", new ExportedWidgetConverter(Flexible::new));
        converters.put("line", new ExportedWidgetConverter(Line::new));
        converters.put("border", new ExportedWidgetConverter(Border::new));
        converters.put("Text", new ExportedWidgetConverter(Text::new));
        converters.put("slot", new ExportedWidgetConverter(Slot::new));
        converters.put("absXY", new ExportedWidgetConverter(AbsXY::new));
        converters.put("Placeholder", new ExportedWidgetConverter(Placeholder::new));
        converters.put("TextField", new ExportedWidgetConverter(TextField::new));
        converters.put("PopupManager", new ExportedWidgetConverter(PopupMgr::new));
        converters.put("AbstractButton", new ExportedWidgetConverter(Button::new));
        converters.put("AbstractScrollBar", new ExportedWidgetConverter(Scrollbar::new));
        converters.put("ColorButton", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/simpleButton.gui")));
        converters.put("SimpleScrollBar", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/simpleScrollBar.gui")));
        converters.put("SimpleHorizontalScrollBar", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/simpleHorizontalScrollBar.gui")));
        converters.put("SimpleVerticalScrollBar", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/simpleVerticalScrollBar.gui")));


        converters.put("TestView", new ExportedWidgetConverter(TestView::new));
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
