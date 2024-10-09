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

package kr.syeyoung.dungeonsguide.mod.stomp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import lombok.Getter;
import net.minecraftforge.common.MinecraftForge;
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
    public StompClient(URI serverUri, final String token) throws InterruptedException {
        super(serverUri);


        addHeader("Authorization", token);
        addHeader("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);

        setConnectionLostTimeout(5);
        logger.info("connecting websocket");
        if (!connectBlocking()) {
            throw new FailedWebSocketConnection("Cant connect to ws");
        }

        logger.info("connected, stomp handshake");
        while(this.stompClientStatus == StompClientStatus.CONNECTING);
        logger.info("fully connected");

        StompManager.getInstance().resetExponentialBackoff();
    }


    @Getter
    private volatile StompClientStatus stompClientStatus = StompClientStatus.CONNECTING;

    @Getter
    private StompPayload errorPayload;

    private ScheduledFuture heartbeat = null;

    private static final ScheduledExecutorService ex = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder()
                    .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
                    .setNameFormat("DG-StompClient-%d").build()));


    private final int clientHeartbeatSendInterval = 30000;
    private final int clientHeartbeatReceiveInterval = 30000;

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        send(new StompPayload().method(StompHeader.CONNECT)
                .header("accept-version","1.2")
                .header("heart-beat", clientHeartbeatSendInterval+","+clientHeartbeatReceiveInterval)
                .header("host",uri.getHost()).getBuilt()
        );
    }

    @Override
    public void onMessage(String message) {
        try {
            if (message.equals("\n")) return;
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
                    break;
                case CONNECTED:
                    stompClientStatus = StompClientStatus.CONNECTED;

                    String serverHeartbeat = payload.headers().get("heart-beat");
                    System.out.println(serverHeartbeat);
                    if (serverHeartbeat != null) {
                        String[] hearbeatsettings = serverHeartbeat.split(",");
                        int serverHeartbeatReceiveInterval = Integer.parseInt(hearbeatsettings[1]);

                        int targetHeartbeatInterval = Integer.max(serverHeartbeatReceiveInterval, clientHeartbeatSendInterval); // umm spec says so lol

                        int serverHeartbeatSendInterval = Integer.parseInt(hearbeatsettings[0]);
                        int targetHeartbeatReceiveInterval = Integer.max(serverHeartbeatSendInterval, clientHeartbeatReceiveInterval);

                        // this doesn't work as intended but it is good enough lol

                        this.heartbeat = ex.scheduleAtFixedRate(() -> send("\n"), targetHeartbeatInterval, targetHeartbeatInterval, TimeUnit.MILLISECONDS);
                    }

                    break;
                case MESSAGE:
                    String subscriptionName = payload.headers().get("subscription");
                    int subscriptionId = Integer.parseInt(subscriptionName);
                    StompSubscription listener = stompSubscriptionMap.get(subscriptionId);

                    listener.process(this, payload.payload());

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
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (heartbeat != null) heartbeat.cancel(true);
//        ex.shutdownNow(); // OHHHHH
        stompClientStatus = StompClientStatus.DISCONNECTED;
        MinecraftForge.EVENT_BUS.post(new StompDiedEvent(code, reason, remote));
        StompManager.getInstance().onStompDied(new StompDiedEvent(code, reason, remote));
    }

    @Override
    public void onError(Exception ex) {
        if(ex != null){
            ex.printStackTrace();
        }
    }


    private final Map<Integer, StompSubscription> stompSubscriptionMap = new HashMap<>();
    private final Map<String, Integer> stompSubscriptionDestMap = new HashMap<>();
    private final Map<Integer, StompPayload> receiptMap = new HashMap<>();

    private int idIncrement = 0;

    private void makeSureStompIsConnected() {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
    }

    public void sendFake(StompPayload payload) {
        makeSureStompIsConnected();
        payload.method(StompHeader.SEND);
        if (payload.headers().get("receipt") != null)
            receiptMap.put(Integer.parseInt(payload.headers().get("receipt")), payload);
        ex.submit(() -> {
            send(payload.getBuilt());
        });
    }

    public int subscribe(String destination, StompSubscription listener) {
        makeSureStompIsConnected();
        int id = ++idIncrement;

        ex.submit(() -> {
            send(new StompPayload()
                            .method(StompHeader.SUBSCRIBE)
                            .header("id", String.valueOf(id))
                            .destination(destination)
                            .header("ack", "auto")
                            .getBuilt()
                    );
                }
        );

        stompSubscriptionMap.put(id, listener);
        stompSubscriptionDestMap.put(destination, id);
        return id;
    }

    public void unsubscribe(String destination) {
        Integer id = stompSubscriptionDestMap.remove(destination);
        if (id == null) return;

        ex.submit(() -> {
                    send(new StompPayload()
                            .method(StompHeader.UNSUBSCRIBE)
                            .header("id", String.valueOf(id))
                            .destination(destination)
                            .header("ack", "auto")
                            .getBuilt()
                    );
        });
        stompSubscriptionMap.remove(id);
    }


    public void disconnect() {
        makeSureStompIsConnected();
        stompClientStatus =StompClientStatus.DISCONNECTING;

        StompPayload stompPayload = new StompPayload().method(StompHeader.DISCONNECT).header("receipt", String.valueOf(++idIncrement));

        receiptMap.put(idIncrement, stompPayload);
        ex.submit(() -> {
                    send(stompPayload.getBuilt());
                });
    }


    public enum  StompClientStatus {
        CONNECTING, CONNECTED, ERROR, DISCONNECTING, DISCONNECTED
    }
}
