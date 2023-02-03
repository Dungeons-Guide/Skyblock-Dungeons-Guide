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
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.data.Parser;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.data.ParserElement;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is for widgets using xml to describe their layout
 */
public abstract class AnnotatedImportOnlyWidget extends Widget implements ImportingWidget {
    private Map<String, BindableAttribute> importedAttributes = null;
    private Map<String, MethodHandle> invocationTargets = null;

    private final ResourceLocation target;

    public AnnotatedImportOnlyWidget(ResourceLocation resourceLocation) {
        target = resourceLocation;
    }

    public final List<Widget> build(DomElement buildContext) {
        try (Parser parser = DomElementRegistry.obtainParser(target)) {
            ParserElement element = parser.getRootNode();
            ParsedWidgetConverter converter = DomElementRegistry.obtainConverter(element.getNodeName());
            Widget w = converter.convert(this, element);
            return Collections.singletonList(w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Map<String, BindableAttribute> getImportedAttributes(Class clazz, Object inst) {
        Map<String, BindableAttribute> attributes = new HashMap<>();
        for (Field declaredField : FieldUtils.getAllFieldsList(clazz)) {
            if (declaredField.getAnnotation(Bind.class) != null) {
                Bind bind = declaredField.getAnnotation(Bind.class);

                if (declaredField.getType() != BindableAttribute.class) throw new IllegalStateException("Bind Annotation must be applied on BindableAttribute field.");
                if (!Modifier.isFinal(declaredField.getModifiers())) throw new IllegalStateException("Bound Bindable Attribute must be final ");

                try {
                    attributes.put(bind.variableName(), (BindableAttribute) declaredField.get(inst));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return attributes;
    }

    protected static Map<String, MethodHandle> getInvocationTargets(Class clazz, Object inst) {
        Map<String, MethodHandle> invocationTargets = new HashMap<>();
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.getAnnotation(On.class) != null) {
                On on = declaredMethod.getAnnotation(On.class);

                try {
                    MethodHandle handle = MethodHandles.publicLookup().unreflect(declaredMethod);
                    invocationTargets.put(on.functionName(), handle);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return invocationTargets;
    }

    private Map<String, BindableAttribute> getImportedAttributes() {
        if (importedAttributes == null)
            importedAttributes = getImportedAttributes(getClass(), this);
        return importedAttributes;
    }

    @Override
    public <T> BindableAttribute<T> getBindTarget(String variableName, BindableAttribute<T> _) {
        return getImportedAttributes().get(variableName);
    }

    private Map<String, MethodHandle> getInvocationTargets() {
        if (invocationTargets == null)
            invocationTargets = getInvocationTargets(getClass(), this);
        return invocationTargets;
    }
    @Override
    public MethodHandle getInvocationTarget(String functionName) {
        return getInvocationTargets().get(functionName);
    }
}


