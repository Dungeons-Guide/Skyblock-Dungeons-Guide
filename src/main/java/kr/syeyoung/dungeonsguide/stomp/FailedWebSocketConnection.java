package kr.syeyoung.dungeonsguide.stomp;

public class FailedWebSocketConnection extends RuntimeException{
    public FailedWebSocketConnection(String message) {
        super(message);
    }
}
