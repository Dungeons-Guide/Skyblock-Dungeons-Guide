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
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.ParserElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.ParserElementList;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class PropByPropParsedWidgetConverter<W extends Widget, R extends Widget & ImportingWidget> implements ParsedWidgetConverter<W, R> {

    public abstract W instantiateWidget();

    public abstract BindableAttribute getExportedAttribute(W widget, String attributeName);

    @Override
    public W convert(R rootWidget, ParserElement element) {
        W partial = instantiateWidget();

        Set<String> boundSlots = new HashSet<>();
        for (String attribute : element.getAttributes()) {
            if (attribute.startsWith("bind:")) {
                String name = attribute.substring(5);
                String variable = element.getAttributeValue(attribute);
                if (name.startsWith("_"))
                    boundSlots.add(name);

                BindableAttribute exported = getExportedAttribute(partial, name);
                if (exported == null) throw new IllegalStateException("No exported variable found named "+name+"!");
                BindableAttribute bound = rootWidget.getBindTarget(variable, exported);
                if (bound == null) throw new IllegalStateException("No bind target found for "+attribute+" for "+variable+"!");
                exported.exportTo(bound);
            } else if (attribute.startsWith("on:")) {
                String name = attribute.substring(3);
                String variable = element.getAttributeValue(attribute);

                BindableAttribute exported = getExportedAttribute(partial, name);
                if (exported == null) throw new IllegalStateException("No exported invocation target found named "+name+"!");
                MethodHandle invocationTarget = rootWidget.getInvocationTarget(variable);
                if (invocationTarget == null) throw new IllegalStateException("No invocationTarget target found for "+attribute+" for "+variable+"!");

                    // convert methodhandle to functional interface.
                    Class functionalInterface = exported.getType();
                    if (!functionalInterface.isInterface()) throw new IllegalArgumentException("Should be interface");
                    if (functionalInterface.getDeclaredMethods().length != 1)
                        throw new IllegalArgumentException("Should be functional interface");
                    Method m = functionalInterface.getDeclaredMethods()[0];

                    MethodType mt = MethodType.methodType(m.getReturnType(), m.getParameterTypes());
                    try {
                        Object obj = LambdaMetafactory.metafactory(MethodHandles.lookup(), m.getName(),
                                        MethodType.methodType(functionalInterface, rootWidget.getClass()),
                                        mt,
                                        invocationTarget,
                                        invocationTarget.type().dropParameterTypes(0, 1))
                                .getTarget()
                                .invoke(rootWidget);
                        exported.setValue(obj);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                // this should bind to methodhandle
            } else if (attribute.equals("slot")) {
            } else {
                BindableAttribute bindableAttribute = getExportedAttribute(partial, attribute);
                if (bindableAttribute == null) throw new IllegalStateException("No exported variable found named "+attribute+"!");
                bindableAttribute.setValue(element.getConvertedAttributeValue(bindableAttribute.getType(), attribute));
            }
        }


        Map<String, List<ParserElement>> children = new HashMap<>();
        children.put("", new LinkedList<>());
        for (ParserElement child : element.getChildren()) {
            String slotName = child.getAttributeValue("slot");
            if (slotName == null) slotName = "";

            if (!children.containsKey(slotName))
                children.put(slotName, new LinkedList<>());
            children.get(slotName).add(child);
        }

        for (Map.Entry<String, List<ParserElement>> stringListEntry : children.entrySet()) {
            if (boundSlots.contains("_"+stringListEntry.getKey())) continue;
            BindableAttribute attribute = getExportedAttribute(partial, "_"+stringListEntry.getKey());
            if (attribute == null) {
                // ???
            } else {
                List<ParserElement> elements = stringListEntry.getValue();
                if (attribute.getType() == ParserElement.class) {
                    if (elements.size() > 1) throw new IllegalArgumentException("More than 1 for single parser element: "+stringListEntry.getKey());
                    if (elements.size() == 1)
                        attribute.setValue(elements.get(0));
                    else
                        attribute.setValue(null);
                } else if (attribute.getType() == Widget.class) {
                    if (elements.size() > 1) throw new IllegalArgumentException("More than 1 for single widget: "+stringListEntry.getKey());
                    if (elements.size() == 1)
                        attribute.setValue(DomElementRegistry.obtainConverter(elements.get(0).getNodename())
                                .convert(rootWidget, elements.get(0)));
                    else attribute.setValue(null);
                } else if (attribute.getType() == ParserElementList.class) {
                    attribute.setValue(elements);
                } else if (attribute.getType() == WidgetList.class) {
                    attribute.setValue(
                            elements.stream()
                                    .map(a -> DomElementRegistry.obtainConverter(a.getNodename()).convert(rootWidget, a))
                                    .collect(Collectors.toList()));
                }
            }
        }

        return partial;
    }
}
