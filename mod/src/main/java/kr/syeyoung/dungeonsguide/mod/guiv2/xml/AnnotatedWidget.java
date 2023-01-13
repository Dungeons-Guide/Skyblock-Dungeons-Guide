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

import kr.syeyoung.dungeonsguide.mod.guiv2.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Passthrough;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Passthroughs;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.Parser;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.ParserElement;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.*;

/**
 * This class is for widgets using xml to describe their layout
 */
public abstract class AnnotatedWidget extends Widget implements ImportingWidget, ExportedWidget {
    private Map<String, BindableAttribute> importedAttributes = null;
    private Map<String, BindableAttribute> exportedAttributes = null;
    private Map<String, MethodHandle> invocationTargets = null;
    @Export(attributeName = "ref")
    public final BindableAttribute<DomElement> ref = new BindableAttribute<>(DomElement.class);


    private final ResourceLocation target;
    public AnnotatedWidget(ResourceLocation location) {
        target = location;
    }

    public DomElement createDomElement(DomElement parent) {
        DomElement domElement = super.createDomElement(parent);
        ref.setValue(domElement);
        return domElement;
    }

    public final List<Widget> build(DomElement buildContext) {
        try (Parser parser = DomElementRegistry.obtainParser(target)) {
            ParserElement element = parser.getRootNode();
            if (element.getNodename().equals("multi")) {
                List<Widget> widgets = new ArrayList<>();
                for (ParserElement child : element.getChildren()) {
                    ParsedWidgetConverter converter = DomElementRegistry.obtainConverter(child.getNodename());
                    Widget w = converter.convert(this, child);
                    widgets.add(w);
                }
                return widgets;
            } else {
                ParsedWidgetConverter converter = DomElementRegistry.obtainConverter(element.getNodename());
                Widget w = converter.convert(this, element);
                return Collections.singletonList(w);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    protected static Pair<Map<String, BindableAttribute>, Map<String, BindableAttribute>> createPassthroughs(Class clazz) {
        if (clazz.getAnnotation(Passthroughs.class) == null && clazz.getAnnotation(Passthrough.class) == null) return Pair.of(Collections.EMPTY_MAP, Collections.EMPTY_MAP);
        Map<String, BindableAttribute> attributeMap1 = new HashMap<>();
        Map<String, BindableAttribute> attributeMap2 = new HashMap<>();
        if (clazz.getAnnotation(Passthroughs.class) != null) {
            Passthrough[] throughs = ((Passthroughs) clazz.getAnnotation(Passthroughs.class)).value();
            for (Passthrough through : throughs) {
                BindableAttribute attribute = new BindableAttribute(through.type());
                attributeMap1.put(through.exportName(), attribute);
                attributeMap2.put(through.bindName(), attribute);
            }
        }
        if (clazz.getAnnotation(Passthrough.class) != null) {
            Passthrough through = (Passthrough) clazz.getAnnotation(Passthrough.class);
            BindableAttribute attribute = new BindableAttribute(through.type());
            attributeMap1.put(through.exportName(), attribute);
            attributeMap2.put(through.bindName(), attribute);
        }
        return Pair.of(attributeMap1, attributeMap2);
    }


    private Map<String, BindableAttribute> getExportedAttributes() {
        if (exportedAttributes == null) {
            exportedAttributes = AnnotatedExportOnlyWidget.getExportedAttributes(getClass(), this);
            importedAttributes = AnnotatedImportOnlyWidget.getImportedAttributes(getClass(), this);

            Pair<Map<String, BindableAttribute>, Map<String, BindableAttribute>> stuff = createPassthroughs(getClass());
            exportedAttributes.putAll(stuff.getLeft());
            importedAttributes.putAll(stuff.getRight());
        }
        return exportedAttributes;
    }

    @Override
    public <T> BindableAttribute<T> getExportedAttribute(String attributeName) {
        return getExportedAttributes().get(attributeName);
    }

    private Map<String, BindableAttribute> getImportedAttributes() {
        if (importedAttributes == null) {
            exportedAttributes = AnnotatedExportOnlyWidget.getExportedAttributes(getClass(), this);
            importedAttributes = AnnotatedImportOnlyWidget.getImportedAttributes(getClass(), this);

            Pair<Map<String, BindableAttribute>, Map<String, BindableAttribute>> stuff = createPassthroughs(getClass());
            exportedAttributes.putAll(stuff.getLeft());
            importedAttributes.putAll(stuff.getRight());
        }
        return importedAttributes;
    }

    @Override
    public <T> BindableAttribute<T> getBindTarget(String variableName, BindableAttribute<T> _) {
        return getImportedAttributes().get(variableName);
    }
    private Map<String, MethodHandle> getInvocationTargets() {
        if (invocationTargets == null)
            invocationTargets = AnnotatedImportOnlyWidget.getInvocationTargets(getClass(), this);
        return invocationTargets;
    }
    @Override
    public MethodHandle getInvocationTarget(String functionName) {
        return getInvocationTargets().get(functionName);
    }


    public void onUnmount() {
        for (BindableAttribute value : getExportedAttributes().values()) {
            value.unexportAll();
        }
    }
}
