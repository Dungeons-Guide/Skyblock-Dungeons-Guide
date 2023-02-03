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

package kr.syeyoung.dungeonsguide.launcher.guiv2.xml;

import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Export;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is for widgets using xml to describe their layout
 */
public abstract class AnnotatedExportOnlyWidget extends Widget implements ExportedWidget {

    private Map<String, BindableAttribute> exportedAttributes = null;

    @Export(attributeName = "ref")
    public final BindableAttribute<DomElement> ref = new BindableAttribute<>(DomElement.class);

    public DomElement createDomElement(DomElement parent) {
        DomElement domElement = super.createDomElement(parent);
        ref.setValue(domElement);
        return domElement;
    }

    public abstract List<Widget> build(DomElement buildContext);

    protected static Map<String, BindableAttribute> getExportedAttributes(Class clazz, Object inst) {
        Map<String, BindableAttribute> attributeMap = new HashMap<>();
        for (Field declaredField : FieldUtils.getAllFields(clazz)) {
            if (declaredField.getAnnotation(Export.class) != null) {
                Export export = declaredField.getAnnotation(Export.class);

                if (declaredField.getType() != BindableAttribute.class) throw new IllegalStateException("Export Annotation must be applied on BindableAttribute field. : "+declaredField.getName());
                if (!Modifier.isFinal(declaredField.getModifiers())) throw new IllegalStateException("Exported Bindable Attribute must be final : "+declaredField.getName());

                try {
                    attributeMap.put(export.attributeName(), (BindableAttribute) declaredField.get(inst));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return attributeMap;
    }



    private Map<String, BindableAttribute> getExportedAttributes() {
        if (exportedAttributes == null)
            exportedAttributes = getExportedAttributes(getClass(), this);
        return exportedAttributes;
    }

    @Override
    public <T> BindableAttribute<T> getExportedAttribute(String attributeName) {
        return getExportedAttributes().get(attributeName);
    }

    public void onUnmount() {
        for (BindableAttribute value : getExportedAttributes().values()) {
            value.unexportAll();
        }
    }
}
