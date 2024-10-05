package kr.syeyoung.dungeonsguide.mod.player;

import kr.syeyoung.dungeonsguide.mod.events.impl.DGPlayerEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGPlayerJoinEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGPlayerQuitEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.mod.stomp.*;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResource;
import lombok.Getter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayerManager {
    public static PlayerManager INSTANCE = new PlayerManager();
    private final Set<UUID> subscribedTo = new HashSet<>();

    @Getter
    private final Map<UUID, Boolean> onlineStatus = new HashMap<>();

    public void publish(JSONObject jsonObject) {
        StompManager.getInstance().send(new StompPayload().method(StompHeader.SEND).header("destination", "/app/player.broadcast").payload(jsonObject.toString()));
    }

    public void ping(UUID uuid) {
        StompManager.getInstance().send(new StompPayload().method(StompHeader.SEND).header("destination", "/app/player.ping").payload(uuid.toString()));
    }

    public void subscribeTo(UUID uuid) {
        if (!subscribedTo.add(uuid)) return;
        StompClient stompInterface = StompManager.getInstance().getStompConnection();
        if (stompInterface == null) return;
        stompInterface.subscribe("/topic/player/"+uuid.toString(), (client, msg) -> {
            JSONObject obj = new JSONObject(msg);
            String type = obj.getString("type");
            if ("joined".equals(type)) {
                MinecraftForge.EVENT_BUS.post(new DGPlayerJoinEvent(uuid));
            } else if ("quit".equals(type)) {
                MinecraftForge.EVENT_BUS.post(new DGPlayerQuitEvent(uuid));
            } else {
                MinecraftForge.EVENT_BUS.post(new DGPlayerEvent(uuid, type, obj.getJSONObject("payload")));
            }
        });
    }

    public void unsubscribe(UUID uuid) {
        if (!subscribedTo.remove(uuid)) return;

        StompClient stompInterface = StompManager.getInstance().getStompConnection();
        if (stompInterface == null) return;
        stompInterface.unsubscribe("/topic/player/"+uuid.toString());
        onlineStatus.remove(uuid.toString());
    }

    @SubscribeEvent
    public void stompConnect(StompConnectedEvent event) {
        event.getStompInterface().subscribe("/user/queue/reply/player.ping", (stompClient ,payload) -> {
            JSONObject object = new JSONObject(payload);
            UUID playeruid = UUID.fromString(object.getString("uuid"));
            boolean online = object.getBoolean("online");
            if (online) MinecraftForge.EVENT_BUS.post(new DGPlayerJoinEvent(playeruid));
            else MinecraftForge.EVENT_BUS.post(new DGPlayerQuitEvent(playeruid));
        });
    }

    @SubscribeEvent
    public void onOnline(DGPlayerJoinEvent event) {
        onlineStatus.put(event.getUuid(), true);
    }
    @SubscribeEvent
    public void onOffline(DGPlayerQuitEvent event) {
        onlineStatus.put(event.getUuid(), false);
    }
}
