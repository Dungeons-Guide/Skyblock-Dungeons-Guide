package kr.syeyoung.modapi.events;

public interface ListenerRegistration<T extends Event> {
    Class<T> getEventClass();
    ListenerPriority getPriority();

}
