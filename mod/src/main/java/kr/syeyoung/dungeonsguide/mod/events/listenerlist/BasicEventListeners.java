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

import kr.syeyoung.dungeonsguide.mod.events.DGEvent;
import kr.syeyoung.dungeonsguide.mod.events.EventListener;
import kr.syeyoung.dungeonsguide.mod.events.EventProcessResult;
import kr.syeyoung.dungeonsguide.mod.events.ListenerPriority;
import kr.syeyoung.dungeonsguide.mod.events.ListenerRegistration;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BasicEventListeners<T extends DGEvent> {
    private final Class<T> clazz;
    private final List<BasicListenerRegistration<? super T>>[] registrations;

    public BasicEventListeners(Class<T> eventClass) {
        this.clazz = eventClass;
        registrations = new List[ListenerPriority.SIZE];
        for (int i = 0; i < registrations.length; i++) {
            registrations[i] = new CopyOnWriteArrayList<>();
        }
    }

    @AllArgsConstructor
    public static class BasicListenerRegistration<T extends DGEvent> implements ListenerRegistration<T> {
        private ListenerPriority priority;
        private EventListener<? super T> invokeTarget;
        private Class<T> eventClass;

        @Override
        public Class<T> getEventClass() {
            return eventClass;
        }

        @Override
        public ListenerPriority getPriority() {
            return priority;
        }
    }

    public void invoke(T event, int priority) {
        Iterator<BasicListenerRegistration<? super T>> iterator = registrations[priority].iterator();
        while (iterator.hasNext()) {
            BasicListenerRegistration<? super T> registration = iterator.next();
            EventProcessResult result = ((EventListener<T>) registration.invokeTarget).onEvent(event);
            if (result == EventProcessResult.REMOVE_LISTENER)
                registrations[priority].remove(registration); // CopyOnWriteArrayList iterator does not support remove
        }
    }

    public BasicListenerRegistration<T> registerEventListener(ListenerPriority priority, EventListener<? super T> invokeTarget) {
        BasicListenerRegistration<T> registration = new BasicListenerRegistration<>(priority, invokeTarget, clazz);
        registrations[priority.getPriority()].add(registration);
        return registration;
    }

    public boolean unregisterEventListener(BasicListenerRegistration<T> registration) {
        return registrations[registration.priority.getPriority()].remove(registration);
    }
}
