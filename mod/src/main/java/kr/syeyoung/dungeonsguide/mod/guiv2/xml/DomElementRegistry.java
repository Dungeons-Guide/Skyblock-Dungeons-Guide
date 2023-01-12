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
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.image.ResourceImage;
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
    
    public static void register(String xmlName, ParsedWidgetConverter converter) {
        converters.put(xmlName, converter);
    }

    public static <T extends Widget, R extends Widget & ImportingWidget> ParsedWidgetConverter<T, R> obtainConverter(String name) {
        return converters.get(name);
    }

    static {
        register("stack", new ExportedWidgetConverter(Stack::new));
        register("size", new ExportedWidgetConverter(SizedBox::new));
        register("scaler", new ExportedWidgetConverter(Scaler::new));
        register("row", new ExportedWidgetConverter(Row::new));
        register("padding", new ExportedWidgetConverter(Padding::new));
        register("col", new ExportedWidgetConverter(Column::new));
        register("bgcolor", new ExportedWidgetConverter(Background::new));
        register("align", new ExportedWidgetConverter(Align::new));
        register("flexible", new ExportedWidgetConverter(Flexible::new));
        register("line", new ExportedWidgetConverter(Line::new));
        register("border", new ExportedWidgetConverter(Border::new));
        register("Text", new ExportedWidgetConverter(Text::new));
        register("slot", new ExportedWidgetConverter(Slot::new));
        register("clip", new ExportedWidgetConverter(Clip::new));
        register("measure", new ExportedWidgetConverter(Measure::new));
        register("UnconstrainedBox", new ExportedWidgetConverter(UnconstrainedBox::new));
        register("absXY", new ExportedWidgetConverter(AbsXY::new));
        register("Placeholder", new ExportedWidgetConverter(Placeholder::new));
        register("TextField", new ExportedWidgetConverter(TextField::new));
        register("PopupManager", new ExportedWidgetConverter(PopupMgr::new));
        register("AbstractButton", new ExportedWidgetConverter(Button::new));
        register("ScrollablePanel", new ExportedWidgetConverter(ScrollablePanel::new));
        register("AbstractScrollBar", new ExportedWidgetConverter(Scrollbar::new));
        register("aspectRatio", new ExportedWidgetConverter(AspectRatioFitter::new));
        register("BareResourceImage", new ExportedWidgetConverter(ResourceImage::new));
        register("TestView", new ExportedWidgetConverter(TestView::new));
        
        register("ColorButton", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/simpleButton.gui")));
        register("SimpleScrollBar", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/simpleScrollBar.gui")));
        register("SimpleHorizontalScrollBar", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/simpleHorizontalScrollBar.gui")));
        register("SimpleVerticalScrollBar", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/simpleVerticalScrollBar.gui")));
        register("SlowList", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/slowlist.gui")));
        
        register("ResourceImage", new DelegatingWidgetConverter(new ResourceLocation("dungeonsguide:gui/ratioResourceImage.gui")));

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
