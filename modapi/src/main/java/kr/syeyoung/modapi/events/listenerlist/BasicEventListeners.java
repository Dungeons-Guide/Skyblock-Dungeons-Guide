package kr.syeyoung.modapi.events.listenerlist;

import kr.syeyoung.modapi.events.Event;
import kr.syeyoung.modapi.events.ListenerPriority;
import kr.syeyoung.modapi.events.EventProcessResult;
import kr.syeyoung.modapi.events.ListenerRegistration;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class BasicEventListeners<T extends Event> {
    private List<BasicListenerRegistration<? super T>>[] registrations;

    public BasicEventListeners() {
        registrations = new List[ListenerPriority.values().length];
        for (int i = 0; i < registrations.length; i++) {
            registrations[i] = new CopyOnWriteArrayList<>();
        }
    }

    @AllArgsConstructor
    public static class BasicListenerRegistration<T extends Event> implements ListenerRegistration<T> {
        private ListenerPriority priority;
        private Function<? super T, EventProcessResult> invokeTarget;
    }

    ;

    public void invoke(T event, int priority) {
        Iterator<BasicListenerRegistration<? super T>> eventRegistration = registrations[priority].iterator();
        while (eventRegistration.hasNext()) {
            EventProcessResult result = eventRegistration.next().invokeTarget.apply(event);
            if (result == EventProcessResult.REMOVE_LISTENER)
                eventRegistration.remove();
        }
    }

    public BasicListenerRegistration<T> registerEventListener(ListenerPriority priority, Function<? super T, EventProcessResult> invokeTarget) {
        int p = priority.getPriority();
        BasicListenerRegistration<T> basicListenerRegistration = new BasicListenerRegistration<>(priority, invokeTarget);
        registrations[p].add(basicListenerRegistration);
        return basicListenerRegistration;
    }

    public boolean unregisterEventListener(BasicListenerRegistration<T> basicListenerRegistration) {
        return registrations[basicListenerRegistration.priority.getPriority()].remove(basicListenerRegistration);
    }
}
