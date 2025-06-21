package kr.syeyoung.modapi.events.listenerlist;

import kr.syeyoung.modapi.events.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BasicEventBus implements EventBus {


    public BasicEventBus() {

    }

    private Map<Class, BasicEventListeners<?>> directEventListeners = new HashMap<>();
    private Map<Class, List<BasicEventListeners<?>>> fireCache = new HashMap<>();

    private <T extends Event> BasicEventListeners<T> getDirectEventListener(Class<T> t) {
        if (directEventListeners.containsKey(t)) return (BasicEventListeners<T>) directEventListeners.get(t);
        directEventListeners.put(t, new BasicEventListeners<T>());
        return (BasicEventListeners<T>) directEventListeners.get(t);
    }

    private <T extends Event> List<BasicEventListeners<? super T>> getFireTarget(Class<T> t) {
        if (fireCache.containsKey(t)) return (List<BasicEventListeners<? super T>>) fireCache.get(t);

        Class c = t;
        List<BasicEventListeners<?>> listeners = new ArrayList<>();
        while (Event.class.isAssignableFrom(c)) {
            listeners.add(getDirectEventListener(c));
            c = c.getSuperclass();
        }

        fireCache.put(t, listeners);
        return (List<BasicEventListeners<? super T>>) listeners;
    }

    @Override
    public <T extends Event> void fireEvent(T t) {
        List<BasicEventListeners<? super T>> eventListeners = this.getFireTarget((Class<T>) t.getClass());
        for (int i = 0; i < ListenerPriority.SIZE; i++) {
            for (BasicEventListeners<? super T> eventListener : eventListeners) {
                eventListener.invoke(t, i);
            }
        }
    }

    @Override
    public <T extends Event> ListenerRegistration<T> registerListener(Class<T> clazz, ListenerPriority priority, Function<T, EventProcessResult> invoke) {
        return getDirectEventListener(clazz).registerEventListener(priority, invoke);
    }

    @Override
    public <T extends Event> boolean unregisterListener(ListenerRegistration<T> registration) {
        return getDirectEventListener(registration.getEventClass()).unregisterEventListener((BasicEventListeners.BasicListenerRegistration<T>) registration);
    }
}
