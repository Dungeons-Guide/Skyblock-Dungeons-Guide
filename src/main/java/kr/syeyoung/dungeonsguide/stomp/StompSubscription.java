package kr.syeyoung.dungeonsguide.stomp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class StompSubscription {
    private int id;
    private String destination;
    private StompMessageHandler stompMessageHandler;
    private AckMode ackMode;

    @AllArgsConstructor
    public enum AckMode {
        AUTO("auto"), CLIENT("client"), CLIENT_INDIVIDUAL("client-individual");

        @Getter
        private final String value;
    }
}
