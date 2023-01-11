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
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.Parser;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.ParserElement;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            ParsedWidgetConverter converter = DomElementRegistry.obtainConverter(element.getNodename());
            Widget w = converter.convert(this, element);
            return Collections.singletonList(w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    private Map<String, BindableAttribute> getExportedAttributes() {
        if (exportedAttributes == null)
            exportedAttributes = AnnotatedExportOnlyWidget.getExportedAttributes(getClass(), this);
        return exportedAttributes;
    }

    @Override
    public <T> BindableAttribute<T> getExportedAttribute(String attributeName) {
        return getExportedAttributes().get(attributeName);
    }

    private Map<String, BindableAttribute> getImportedAttributes() {
        if (importedAttributes == null)
            importedAttributes = AnnotatedImportOnlyWidget.getImportedAttributes(getClass(), this);
        return importedAttributes;
    }

    @Override
    public <T> BindableAttribute<T> getBindTarget(String variableName) {
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
