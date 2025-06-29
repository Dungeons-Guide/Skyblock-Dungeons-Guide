package kr.syeyoung.modapi.event;

public interface EventBus {

    <T extends UEvent> boolean fireEvent(T t);
    <T extends UEvent> boolean fireEvent(T t, ListenerPriority priority);

    <T extends UEvent> ListenerRegistration<T> registerListener(Class<T> clazz, ListenerPriority priority, EventListener<T> invoke);
    <T extends UEvent> boolean unregisterListener(ListenerRegistration<T> registration);
}
