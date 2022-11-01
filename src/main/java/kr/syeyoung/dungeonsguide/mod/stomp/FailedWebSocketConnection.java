package kr.syeyoung.dungeonsguide.mod.stomp;

public class FailedWebSocketConnection extends RuntimeException{
    public FailedWebSocketConnection(String message) {
        super(message);
    }
}
