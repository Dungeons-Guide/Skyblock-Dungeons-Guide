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

package kr.syeyoung.dungeonsguide.mod.events.listenerlist;

import kr.syeyoung.dungeonsguide.mod.events.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BasicEventBus implements EventBus {
    private final Map<Class, BasicEventListeners<?>> directEventListeners = new ConcurrentHashMap<>();
    private final Map<Class, List<BasicEventListeners<?>>> fireCache = new ConcurrentHashMap<>();

    private <T extends DGEvent> BasicEventListeners<T> getDirectEventListener(Class<T> t) {
        return (BasicEventListeners<T>) directEventListeners.computeIfAbsent(t, BasicEventListeners::new);
    }

    // listeners of the event class and all of its superclasses
    private <T extends DGEvent> List<BasicEventListeners<? super T>> getFireTarget(Class<T> t) {
        return (List) fireCache.computeIfAbsent(t, a -> {
            List<BasicEventListeners<?>> listeners = new ArrayList<>();
            Class c = a;
            while (DGEvent.class.isAssignableFrom(c)) {
                listeners.add(getDirectEventListener(c));
                c = c.getSuperclass();
            }
            return listeners;
        });
    }

    @Override
    public <T extends DGEvent> boolean fireEvent(T t) {
        List<BasicEventListeners<? super T>> eventListeners = getFireTarget((Class<T>) t.getClass());
        for (int i = 0; i < ListenerPriority.SIZE; i++) {
            for (BasicEventListeners<? super T> eventListener : eventListeners) {
                ((BasicEventListeners<T>) eventListener).invoke(t, i);
            }
        }
        return t instanceof Cancelable && ((Cancelable) t).isCanceled();
    }

    @Override
    public <T extends DGEvent> boolean fireEvent(T t, ListenerPriority priority) {
        List<BasicEventListeners<? super T>> eventListeners = getFireTarget((Class<T>) t.getClass());
        for (BasicEventListeners<? super T> eventListener : eventListeners) {
            ((BasicEventListeners<T>) eventListener).invoke(t, priority.getPriority());
        }
        return t instanceof Cancelable && ((Cancelable) t).isCanceled();
    }

    @Override
    public <T extends DGEvent> ListenerRegistration<T> registerListener(Class<T> clazz, ListenerPriority priority, EventListener<T> invoke) {
        return getDirectEventListener(clazz).registerEventListener(priority, invoke);
    }

    @Override
    public <T extends DGEvent> boolean unregisterListener(ListenerRegistration<T> registration) {
        return getDirectEventListener(registration.getEventClass()).unregisterEventListener((BasicEventListeners.BasicListenerRegistration<T>) registration);
    }
}
