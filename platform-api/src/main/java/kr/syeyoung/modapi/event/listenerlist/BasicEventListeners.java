package kr.syeyoung.modapi.event.listenerlist;

import kr.syeyoung.modapi.event.*;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BasicEventListeners<T extends UEvent> {
    private Class<T> clazz;
    private List<BasicListenerRegistration<? super T>>[] registrations;

    public BasicEventListeners(Class<T> eventClass) {
        this.clazz = eventClass;
        registrations = new List[ListenerPriority.values().length];
        for (int i = 0; i < registrations.length; i++) {
            registrations[i] = new CopyOnWriteArrayList<>();
        }
    }

    @AllArgsConstructor
    public static class BasicListenerRegistration<T extends UEvent> implements ListenerRegistration<T> {
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

    ;

    public void invoke(T event, int priority) {
        Iterator<BasicListenerRegistration<? super T>> eventRegistration = registrations[priority].iterator();
        while (eventRegistration.hasNext()) {
            EventProcessResult result = eventRegistration.next().invokeTarget.onEvent(event);
            if (result == EventProcessResult.REMOVE_LISTENER)
                eventRegistration.remove();
        }
    }

    public BasicListenerRegistration<T> registerEventListener(ListenerPriority priority, EventListener<? super T> invokeTarget) {
        int p = priority.getPriority();
        BasicListenerRegistration<T> basicListenerRegistration = new BasicListenerRegistration<>(priority, invokeTarget, clazz);
        registrations[p].add(basicListenerRegistration);
        return basicListenerRegistration;
    }

    public boolean unregisterEventListener(BasicListenerRegistration<T> basicListenerRegistration) {
        return registrations[basicListenerRegistration.priority.getPriority()].remove(basicListenerRegistration);
    }
}
