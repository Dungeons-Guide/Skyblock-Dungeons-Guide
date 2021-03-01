package kr.syeyoung.dungeonsguide.stomp;

public interface StompMessageHandler {
    void handle(StompInterface stompInterface, StompPayload stompPayload);
}
