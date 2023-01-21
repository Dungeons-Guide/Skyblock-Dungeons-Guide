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

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.Parser;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.ParserElement;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DelegatingWidget extends Widget implements ExportedWidget, ImportingWidget {

    private final List<Widget> widgets;
    public DelegatingWidget(ResourceLocation location) {


        try (Parser parser = DomElementRegistry.obtainParser(location)) {
            ParserElement element = parser.getRootNode();
            if (!element.getNodename().equals("wrapper")) throw new IllegalArgumentException("Delegating widget root element Must be wrapper");
            List<Widget> widgets = new ArrayList<>();
            for (ParserElement child : element.getChildren()) {
                ParsedWidgetConverter converter = DomElementRegistry.obtainConverter(child.getNodename());
                Widget w = converter.convert(this, child);
                widgets.add(w);
            }
            this.widgets = widgets;


            for (String attribute : element.getAttributes()) {
                getExportedAttribute(attribute).setValue(
                        StringConversions.convert(getExportedAttribute(attribute).getType(), element.getAttributeValue(attribute))
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        exportedAttributes.put("ref", ref);
    }

    private final Map<String, BindableAttribute> exportedAttributes = new HashMap<>();

    private BindableAttribute<DomElement> ref = new BindableAttribute<>(DomElement.class);


    @Override
    public <T> BindableAttribute<T> getExportedAttribute(String attributeName) {
        return exportedAttributes.get(attributeName);
    }
    @Override
    public <T> BindableAttribute<T> getBindTarget(String variableName, BindableAttribute<T> from) {
        if (exportedAttributes.containsKey(variableName))
            return exportedAttributes.get(variableName);
        BindableAttribute<T> newThing = new BindableAttribute<T>(from.getType());
        exportedAttributes.put(variableName, newThing);
        return newThing;
    }

    @Override
    public MethodHandle getInvocationTarget(String functionName) {
        throw new UnsupportedOperationException("lol");
    }

    @Override
    public DomElement createDomElement(DomElement parent) {
        DomElement domElement =  super.createDomElement(parent);
        ref.setValue(domElement);
        return domElement;
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return widgets;
    }
}
