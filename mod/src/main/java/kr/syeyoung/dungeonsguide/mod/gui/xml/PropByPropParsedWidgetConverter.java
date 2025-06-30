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

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.ParserElement;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.ParserElementList;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.*;

public abstract class PropByPropParsedWidgetConverter<W extends Widget, R extends Widget & ImportingWidget> implements ParsedWidgetConverter<W, R> {

    public abstract W instantiateWidget(ParserElement parserElement);

    public abstract BindableAttribute getExportedAttribute(W widget, String attributeName);

    private static Map<SamCacheKey, CallSite> samCache = new HashMap<SamCacheKey, CallSite>();
    @EqualsAndHashCode @AllArgsConstructor
    private static class SamCacheKey {
        Class functionalInterface;
        MethodHandle invokeTarget;
    }
    private static CallSite createLambda(Class functionalInterface, MethodHandle invokeTarget, Class targetOwner) {
        SamCacheKey key = new SamCacheKey(functionalInterface, invokeTarget);
        CallSite res = samCache.get(key);
        if (res != null) return res;


        if (!functionalInterface.isInterface()) throw new IllegalArgumentException("Should be interface");
        if (functionalInterface.getDeclaredMethods().length != 1)
            throw new IllegalArgumentException("Should be functional interface");
        Method m = functionalInterface.getDeclaredMethods()[0];

        MethodType mt = MethodType.methodType(m.getReturnType(), m.getParameterTypes());

        try {
            res = LambdaMetafactory.metafactory(MethodHandles.lookup(), m.getName(),
                    MethodType.methodType(functionalInterface, targetOwner),
                    mt,
                    invokeTarget,
                    invokeTarget.type().dropParameterTypes(0, 1));
            samCache.put(key, res);
            return res;
        } catch (LambdaConversionException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public W convert(R rootWidget, ParserElement element) {
        W partial = instantiateWidget(element);

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

                try {
                    Object lambda = createLambda(functionalInterface, invocationTarget, rootWidget.getClass())
                            .getTarget().invoke(rootWidget);
                    exported.setValue(lambda);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                // this should bind to methodhandle
            } else if (attribute.equals("slot") || attribute.equals("include")) {
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
                        attribute.setValue(DomElementRegistry.obtainConverter(elements.get(0).getNodeName())
                                .convert(rootWidget, elements.get(0)));
                    else attribute.setValue(null);
                } else if (attribute.getType() == ParserElementList.class) {
                    attribute.setValue(elements);
                } else if (attribute.getType() == WidgetList.class) {
                    List<Widget> widgets = new ArrayList<>();
                    for (ParserElement parserElement : elements) {
                        Widget w = DomElementRegistry.obtainConverter(parserElement.getNodeName()).convert(rootWidget, parserElement);
                        widgets.add(w);
                    }
                    attribute.setValue(widgets);
                }
            }
        }

        return partial;
    }
}
