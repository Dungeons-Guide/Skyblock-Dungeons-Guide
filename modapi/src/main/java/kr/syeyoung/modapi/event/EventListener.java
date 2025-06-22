package kr.syeyoung.modapi.event;

public interface EventListener<T extends UEvent> {
    EventProcessResult onEvent(T event);
}
