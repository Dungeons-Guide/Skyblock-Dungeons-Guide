package kr.syeyoung.modapi.events;

import java.util.function.Function;

public interface EventBus {

    <T extends Event> void fireEvent(T t);

    <T extends Event> ListenerRegistration<T> registerListener(Class<T> clazz, ListenerPriority priority, Function<T, EventProcessResult> invoke);
    <T extends Event> boolean unregisterListener(ListenerRegistration<T> registration);
}
