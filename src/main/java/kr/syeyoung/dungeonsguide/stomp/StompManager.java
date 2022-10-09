package kr.syeyoung.dungeonsguide.stomp;

import com.google.common.base.Throwables;
import kr.syeyoung.dungeonsguide.auth.AuthManager;
import kr.syeyoung.dungeonsguide.events.impl.StompConnectedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StompManager {
    Logger logger = LogManager.getLogger("StompManager");
    public static final String STOMP_URL = "wss://dungeons.guide/ws";
    //    private String stompURL = "ws://localhost/ws";
    static StompManager instance;

    public static StompManager getInstance() {
        if (instance == null) {
            instance = new StompManager();
            MinecraftForge.EVENT_BUS.register(instance);
        }
        return instance;
    }

    public void init() {
        connectStomp();
    }

    private StompClient stompConnection;


    public boolean isStompConnected(){
        if(stompConnection != null && stompConnection.getStompClientStatus() == StompClient.StompClientStatus.CONNECTED) return true;
        return false;
    }

    public void send(StompPayload payload){
        if(stompConnection != null){
            stompConnection.sendfake(payload);
        } else {
            logger.error("OOPS STOMP CONNECTION IS NULL AND SOMEONE TRIED TO SEND SOMETHING THIS SHOULD NOT HAPPEN");
        }
    }

    ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();

    @SubscribeEvent
    public void onStompDied(StompDiedEvent event) {
        logger.info("Stomp Connection closed, trying to reconnect - {} - {}", event.reason, event.code);
        connectStomp();
    }

    public void connectStomp() {
        ex.schedule(() -> {
            if (AuthManager.getInstance().getToken() == null) return;
            try {
                if (stompConnection != null) {
                    stompConnection.disconnect();
                }
                stompConnection = new StompClient(new URI(StompManager.STOMP_URL), AuthManager.getInstance().getToken());
                MinecraftForge.EVENT_BUS.post(new StompConnectedEvent(stompConnection));
            } catch (Exception e) {
                logger.error("Failed to connect to Stomp with message: {}", String.valueOf(Throwables.getRootCause(e)));
            }

        }, 5L, TimeUnit.SECONDS);
    }
}
