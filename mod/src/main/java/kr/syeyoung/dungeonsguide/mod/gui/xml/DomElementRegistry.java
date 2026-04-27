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

package kr.syeyoung.dungeonsguide.mod.gui.xml;

import kr.syeyoung.dungeonsguide.mod.config.onboarding.OnboardingCard;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.*;
import kr.syeyoung.dungeonsguide.mod.gui.elements.image.ResourceImage;
import kr.syeyoung.dungeonsguide.mod.gui.elements.image.URLImage;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.HoverTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.view.TestView;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.Parser;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.ParserException;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.W3CBackedParser;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.resources.UResource;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class DomElementRegistry {
    private static final Map<String, ParsedWidgetConverter> converters = new HashMap<>();
    
    public static void register(String xmlName, ParsedWidgetConverter converter) {
        converters.put(xmlName, converter);
    }

    public static <T extends Widget, R extends Widget & ImportingWidget> ParsedWidgetConverter<T, R> obtainConverter(String name) {
        if (!converters.containsKey(name)) {
            System.out.println("Try to get nonexistent widget " + name);


            try {
                Class clazz = Class.forName(name);
                if (ExportedWidget.class.isAssignableFrom(clazz)) {
                    MethodHandle handle = MethodHandles.publicLookup().unreflectConstructor(clazz.getConstructor());
                    converters.put(name, new ExportedWidgetConverter(() -> {
                        try {
                            return handle.invoke();
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }


        return converters.get(name);
    }

    static {
        register("stack", new ExportedWidgetConverter(Stack::new));
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
        register("ConstrainedBox", new ExportedWidgetConverter(ConstrainedBox::new));
        register("UnconstrainedBox", new ExportedWidgetConverter(UnconstrainedBox::new));
        register("absXY", new ExportedWidgetConverter(AbsXY::new));
        register("Placeholder", new ExportedWidgetConverter(Placeholder::new));
        register("TextField", new ExportedWidgetConverter(TextField::new));
        register("ValidatingTextField", new ExportedWidgetConverter(ValidatingTextField::new));
        register("PopupManager", new ExportedWidgetConverter(PopupMgr::new));
        register("AbstractButton", new ExportedWidgetConverter(Button::new));
        register("AbstractToggleButton", new ExportedWidgetConverter(ToggleButton::new));
        register("ScrollablePanel", new ExportedWidgetConverter(ScrollablePanel::new));
        register("AbstractScrollBar", new ExportedWidgetConverter(Scrollbar::new));
        register("aspectRatio", new ExportedWidgetConverter(AspectRatioFitter::new));
        register("BareResourceImage", new ExportedWidgetConverter(ResourceImage::new));
        register("IntrinsicWidth", new ExportedWidgetConverter(IntrinsicWidth::new));
        register("IntrinsicHeight", new ExportedWidgetConverter(IntrinsicHeight::new));
        register("TestView", new ExportedWidgetConverter(TestView::new));
        register("RoundRect", new ExportedWidgetConverter(RoundRect::new));
        register("CircularRect", new ExportedWidgetConverter(CircularRect::new));
        register("Navigator", new ExportedWidgetConverter(Navigator::new));
        register("Stencil", new ExportedWidgetConverter(Stencil::new));
        register("InvertStencil", new ExportedWidgetConverter(NegativeStencil::new));
        register("WrapGrid", new ExportedWidgetConverter(Wrap::new));

        register("ColorButton", new DelegatingWidgetConverter(new ResourceIdentifier("dungeonsguide:gui/elements/simple_button.gui")));
        register("RoundButton", new DelegatingWidgetConverter(new ResourceIdentifier("dungeonsguide:gui/elements/dg_button.gui")));
        register("IconButton", new DelegatingWidgetConverter(new ResourceIdentifier("dungeonsguide:gui/elements/icon_button.gui")));
        register("SimpleToggleButton", new DelegatingWidgetConverter(new ResourceIdentifier("dungeonsguide:gui/elements/simple_toggle_button.gui")));
        register("SimpleHorizontalScrollBar", new DelegatingWidgetConverter(new ResourceIdentifier("dungeonsguide:gui/elements/simple_horizontal_scroll_bar.gui")));
        register("SimpleVerticalScrollBar", new DelegatingWidgetConverter(new ResourceIdentifier("dungeonsguide:gui/elements/simple_vertical_scroll_bar.gui")));
        register("SlowList", new DelegatingWidgetConverter(new ResourceIdentifier("dungeonsguide:gui/elements/slowlist.gui")));
        register("size", new DelegatingWidgetConverter(new ResourceIdentifier("dungeonsguide:gui/elements/size.gui")));
        register("ResourceImage", new DelegatingWidgetConverter(new ResourceIdentifier("dungeonsguide:gui/elements/ratio_resource_image.gui")));
        register("UrlImage", new ExportedWidgetConverter(URLImage::new));
        register("SelectiveContainer", new ExportedWidgetConverter(SelectiveContainer::new));
        register("ItemStack", new ExportedWidgetConverter(ItemStackRender::new));
        register("Passthrough", new ExportedWidgetConverter(Passthrough::new));
        register("Include", new DelegatingWidgetConverter(null));
        register("Variable", new ExportedWidgetConverter(Variable::new));

        register("OnboardingCard", new ExportedWidgetConverter(OnboardingCard::new)); // ugh.  don't like this.
        register("HoverTooltip", new ExportedWidgetConverter(HoverTooltip::new));
    }

    private static final Map<ResourceIdentifier, Parser> cache = new HashMap<>();

    public static Parser obtainParser(ResourceIdentifier resourceLocation) {
        if (cache.containsKey(resourceLocation)) return cache.get(resourceLocation);
        try {
            UResource iResource = ModAPI.getAPI().getResourceManager().getResource(resourceLocation);
            W3CBackedParser parser = new W3CBackedParser(iResource.getInputStream());
            cache.put(resourceLocation, parser);
            return parser;
        } catch (Exception e) {
            throw new ParserException("An error occurred while parsing "+resourceLocation, e);
        }
    }

    public static void onResourceManagerReload() {
        cache.clear();
    }
}
