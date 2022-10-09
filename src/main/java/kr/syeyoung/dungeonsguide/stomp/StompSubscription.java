package kr.syeyoung.dungeonsguide.stomp;

@FunctionalInterface
public interface StompSubscription {
    void process(StompClient stompInterface,String stompPayload);
}
