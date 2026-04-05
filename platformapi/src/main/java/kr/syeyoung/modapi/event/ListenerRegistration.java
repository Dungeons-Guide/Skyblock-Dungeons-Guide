package kr.syeyoung.modapi.event;

public interface ListenerRegistration<T extends UEvent> {
    Class<T> getEventClass();
    ListenerPriority getPriority();

}
