/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.events.annotations;

import kr.syeyoung.dungeonsguide.mod.events.*;
import lombok.AllArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class AnnotatedListenerHelper {
    @AllArgsConstructor
    private static class AnnotatedListener<T extends DGEvent> implements EventListener<T> {
        private MethodHandle methodHandle;
        private boolean receiveCanceled;
        private String name;

        @Override
        public EventProcessResult onEvent(T event) {
            if (!receiveCanceled && event instanceof Cancelable && ((Cancelable) event).isCanceled())
                return EventProcessResult.COMPLETE;
            try {
                methodHandle.invoke(event);
            } catch (Throwable e) {
                throw new RuntimeException("An error occurred while handling event at "+name, e);
            }
            return EventProcessResult.COMPLETE;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static List<ListenerRegistration> registerListeners(EventBus eventBus, Object o) {
        List<ListenerRegistration> registrations = new ArrayList<>();
        for (Method declaredMethod : o.getClass().getDeclaredMethods()) {
            SubscribeEvent subscribeEvent = declaredMethod.getAnnotation(SubscribeEvent.class);
            if (subscribeEvent == null) continue;
            if (declaredMethod.getParameterCount() != 1)
                throw new IllegalArgumentException("Method "+declaredMethod.getName()+" should only take in 1 parameter that extends DGEvent");
            Class type = declaredMethod.getParameterTypes()[0];
            if (!DGEvent.class.isAssignableFrom(type))
                throw new IllegalArgumentException("Method "+declaredMethod.getName()+" should only take in 1 parameter that extends DGEvent");
            if ((declaredMethod.getModifiers() & Modifier.PUBLIC) == 0)
                throw new IllegalArgumentException("Method "+declaredMethod.getName()+" is not public");

            MethodHandle handle;
            try {
                handle = MethodHandles.publicLookup().unreflect(declaredMethod).bindTo(o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            registrations.add(eventBus.registerListener(type, subscribeEvent.priority(),
                    new AnnotatedListener(handle, subscribeEvent.receiveCanceled(), o.getClass().getName()+"."+declaredMethod.getName()+"("+type.getName()+")")));
        }
        return registrations;
    }
}
