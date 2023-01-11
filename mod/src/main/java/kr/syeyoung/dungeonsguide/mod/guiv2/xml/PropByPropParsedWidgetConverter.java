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

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public abstract class PropByPropParsedWidgetConverter<W extends Widget, R extends Widget & ImportingWidget> implements ParsedWidgetConverter<W, R> {

    public abstract W instantiateWidget();

    public abstract BindableAttribute getExportedAttribute(W widget, String attributeName);

    @Override
    public W convert(R rootWidget, ParserElement element) {
        W partial = instantiateWidget();

        for (String attribute : element.getAttributes()) {
            if (attribute.startsWith("bind:")) {
                String name = attribute.substring(5);
                String variable = element.getAttributeValue(attribute);

                BindableAttribute exported = getExportedAttribute(partial, name);
                BindableAttribute bound = rootWidget.getBindTarget(variable);

                exported.exportTo(bound);
            } else if (attribute.startsWith("on:")) {
                String name = attribute.substring(3);
                String variable = element.getAttributeValue(attribute);

                BindableAttribute exported = getExportedAttribute(partial, name);
                MethodHandle invocationTarget = rootWidget.getInvocationTarget(variable);

                // convert methodhandle to functional interface.
                Class functionalInterface = exported.getType();
                if (!functionalInterface.isInterface()) throw new IllegalArgumentException("Should be interface");
                if (functionalInterface.getDeclaredMethods().length != 1) throw new IllegalArgumentException("Should be functional interface");
                Method m = functionalInterface.getDeclaredMethods()[0];

                MethodType mt = MethodType.methodType(m.getReturnType(), m.getParameterTypes());
                try {
                    Object obj = LambdaMetafactory.metafactory(MethodHandles.publicLookup(), m.getName(),
                                    MethodType.methodType(functionalInterface),
                            mt.generic(), invocationTarget, mt).getTarget().invokeExact();
                    exported.setValue(obj);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }


                // this should bind to methodhandle
            } else {
                BindableAttribute bindableAttribute = getExportedAttribute(partial, attribute);
                bindableAttribute.setValue(element.getConvertedAttributeValue(bindableAttribute.getType(), attribute));
            }
        }
        return partial;
    }
}
