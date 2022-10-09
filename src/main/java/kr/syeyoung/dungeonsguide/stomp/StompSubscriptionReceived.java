package kr.syeyoung.dungeonsguide.stomp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StompSubscriptionReceived {
    StompClient stompInterface;
    StompPayload stompPayload;
}
