package kr.syeyoung.dungeonsguide.stomp;

public interface StompInterface {
    void send(StompPayload payload);
    void subscribe(StompSubscription stompSubscription);
    void unsubscribe(StompSubscription stompSubscription);
    void disconnect();
}
