package kr.syeyoung.dungeonsguide.stomp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public enum StompHeader {
    SEND, SUBSCRIBE, UNSUBSCRIBE, BEGIN, COMMIT, ABORT, ACK, NACK, DISCONNECT, CONNECT, STOMP, CONNECTED, MESSAGE, RECEIPT, ERROR
}
