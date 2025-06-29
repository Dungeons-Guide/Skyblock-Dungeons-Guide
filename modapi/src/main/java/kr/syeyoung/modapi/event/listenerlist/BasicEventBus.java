package kr.syeyoung.modapi.event.listenerlist;

import kr.syeyoung.modapi.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicEventBus implements EventBus {


    public BasicEventBus() {

    }

    private Map<Class, BasicEventListeners<?>> directEventListeners = new HashMap<>();
    private Map<Class, List<BasicEventListeners<?>>> fireCache = new HashMap<>();

    private <T extends UEvent> BasicEventListeners<T> getDirectEventListener(Class<T> t) {
        if (directEventListeners.containsKey(t)) return (BasicEventListeners<T>) directEventListeners.get(t);
        directEventListeners.put(t, new BasicEventListeners<T>(t));
        return (BasicEventListeners<T>) directEventListeners.get(t);
    }

    private <T extends UEvent> List<BasicEventListeners<? super T>> getFireTarget(Class<T> t) {
        if (fireCache.containsKey(t)) return (List<BasicEventListeners<? super T>>) ((List) fireCache.get(t));

        Class c = t;
        List<BasicEventListeners<?>> listeners = new ArrayList<>();
        while (UEvent.class.isAssignableFrom(c)) {
            listeners.add(getDirectEventListener(c));
            c = c.getSuperclass();
        }

        fireCache.put(t, listeners);
        return (List<BasicEventListeners<? super T>>) ((List) listeners);
    }

    @Override
    public <T extends UEvent> boolean fireEvent(T t) {
        List<BasicEventListeners<? super T>> eventListeners = this.getFireTarget((Class<T>) t.getClass());
        for (int i = 0; i < ListenerPriority.SIZE; i++) {
            for (BasicEventListeners<? super T> eventListener : eventListeners) {
                eventListener.invoke(t, i);
            }
        }

        return t instanceof Cancelable && ((Cancelable) t).isCanceled();
    }

    @Override
    public <T extends UEvent> boolean fireEvent(T t, ListenerPriority priority) {
        List<BasicEventListeners<? super T>> eventListeners = this.getFireTarget((Class<T>) t.getClass());
        for (BasicEventListeners<? super T> eventListener : eventListeners) {
            eventListener.invoke(t, priority.getPriority());
        }
        return t instanceof Cancelable && ((Cancelable) t).isCanceled();
    }

    @Override
    public <T extends UEvent> ListenerRegistration<T> registerListener(Class<T> clazz, ListenerPriority priority, EventListener<T> invoke) {
        return getDirectEventListener(clazz).registerEventListener(priority, invoke);
    }

    @Override
    public <T extends UEvent> boolean unregisterListener(ListenerRegistration<T> registration) {
        return getDirectEventListener(registration.getEventClass()).unregisterEventListener((BasicEventListeners.BasicListenerRegistration<T>) registration);
    }
}
