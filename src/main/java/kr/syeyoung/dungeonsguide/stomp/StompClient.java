/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.stomp;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StompClient extends WebSocketClient {

    Logger logger = LogManager.getLogger("StompClient");
    public StompClient(URI serverUri, final String token, CloseListener closeListener) throws InterruptedException {
        super(serverUri);
        this.closeListener = closeListener;


        addHeader("Authorization", token);

        logger.info("connecting websocket");
        if (!connectBlocking()) {
            throw new RuntimeException("Can't connect to ws");
        }
        logger.info("connected, stomp handshake");
        while(this.stompClientStatus == StompClientStatus.CONNECTING);
        logger.info("fully connected");
    }
    private final CloseListener closeListener;

    @Getter
    private volatile StompClientStatus stompClientStatus = StompClientStatus.CONNECTING;

    @Getter
    private StompPayload errorPayload;

    private ScheduledFuture heartbeat = null;

    private static final ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send(new StompPayload().method(StompHeader.CONNECT)
                .header("accept-version","1.2")
                .header("heart-beat", "30000,30000")
                .header("host",uri.getHost()).getBuilt()
        );
    }

    @Override
    public void onMessage(String message) {
        try {
            StompPayload payload = StompPayload.parse(message);

            switch (payload.method()){
                case SEND:
                case SUBSCRIBE:
                case UNSUBSCRIBE:
                case BEGIN:
                case COMMIT:
                case ABORT:
                case ACK:
                case NACK:
                case DISCONNECT:
                case STOMP:
                case CONNECTED:
                    break;
                case CONNECT:
                    stompClientStatus = StompClientStatus.CONNECTED;

                    String heartbeat = payload.headers().get("heart-beat");
                    if (heartbeat != null) {
//                    int sx = Integer.parseInt(heartbeat.split(",")[0]);
//                    int sy = Integer.parseInt(heartbeat.split(",")[1]);
//
//                    if (sy == 0) return;
                        int heartbeatMS = 30000;
                        this.heartbeat = ex.scheduleAtFixedRate(() -> {
                            send("\n");
                        }, heartbeatMS-1000, heartbeatMS-1000, TimeUnit.MILLISECONDS);
                    }
                    break;

                case MESSAGE:
                    // mesage
                    StompSubscription stompSubscription = stompSubscriptionMap.get(Integer.parseInt(payload.headers().get("subscription")));
                    try {
                        stompSubscription.getStompMessageHandler().handle(this, payload);
                        if (stompSubscription.getAckMode() != StompSubscription.AckMode.AUTO) {
                            send(new StompPayload().method(StompHeader.ACK)
                                    .header("id",payload.headers().get("ack")).getBuilt()
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (stompSubscription.getAckMode() != StompSubscription.AckMode.AUTO) {
                            send(new StompPayload().method(StompHeader.NACK)
                                    .header("id",payload.headers().get("ack")).getBuilt()
                            );
                        }
                    }
                    break;
                case RECEIPT:
                    String receiptId = payload.headers().get("receipt-id");
                    StompPayload payload1 = receiptMap.remove(Integer.parseInt(receiptId));
                    if (payload1.method() == StompHeader.DISCONNECT) {
                        stompClientStatus = StompClientStatus.DISCONNECTED;
                        close();
                    }
                    break;
                case ERROR:
                    errorPayload = payload;
                    stompClientStatus = StompClientStatus.ERROR;
                    this.close();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (heartbeat != null) heartbeat.cancel(true);
        closeListener.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    private final Map<Integer, StompSubscription> stompSubscriptionMap = new HashMap<>();
    private final Map<Integer, StompPayload> receiptMap = new HashMap<>();

    private int idIncrement = 0;

    public void send(StompPayload payload) {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        payload.method(StompHeader.SEND);
        if (payload.headers().get("receipt") != null)
            receiptMap.put(Integer.parseInt(payload.headers().get("receipt")), payload);
        send(payload.getBuilt());
    }

    public void subscribe(StompSubscription stompSubscription) {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        stompSubscription.setId(++idIncrement);

        send(new StompPayload().method(StompHeader.SUBSCRIBE)
                .header("id",String.valueOf(stompSubscription.getId()))
                .header("destination", stompSubscription.getDestination())
                .header("ack", stompSubscription.getAckMode().getValue()).getBuilt()
        );

        stompSubscriptionMap.put(stompSubscription.getId(), stompSubscription);
    }

    public void unsubscribe(StompSubscription stompSubscription) {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        send(new StompPayload().method(StompHeader.UNSUBSCRIBE)
                .header("id",String.valueOf(stompSubscription.getId())).getBuilt()
        );
        stompSubscriptionMap.remove(stompSubscription.getId());
    }

    public void disconnect() {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        stompClientStatus =StompClientStatus.DISCONNECTING;

        StompPayload stompPayload = new StompPayload().method(StompHeader.DISCONNECT).header("receipt", String.valueOf(++idIncrement));

        send(stompPayload.getBuilt());
        receiptMap.put(idIncrement, stompPayload);
    }

    public enum  StompClientStatus {
        CONNECTING, CONNECTED, ERROR, DISCONNECTING, DISCONNECTED
    }
}
