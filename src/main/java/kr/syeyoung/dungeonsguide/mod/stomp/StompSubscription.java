package kr.syeyoung.dungeonsguide.mod.stomp;

@FunctionalInterface
public interface StompSubscription {
    void process(StompClient stompInterface, String stompPayload);
}
