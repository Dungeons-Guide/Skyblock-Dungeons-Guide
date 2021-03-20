package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.StompConnectedListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.stomp.StompInterface;
import kr.syeyoung.dungeonsguide.stomp.StompMessageHandler;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.stomp.StompSubscription;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FeatureUpdateAlarm extends SimpleFeature implements StompConnectedListener, StompMessageHandler, TickListener {
    public FeatureUpdateAlarm() {
        super("ETC", "Update Alarm","Show a warning on chat when new update has bnee released.", "etc.updatealarm", true);
    }

    private StompPayload stompPayload;
    @Override
    public void handle(StompInterface stompInterface, StompPayload stompPayload) {
        this.stompPayload = stompPayload;
    }

    @Override
    public void onTick() {
        if (stompPayload != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(stompPayload.payload()));
            stompPayload = null;
            Minecraft.getMinecraft().thePlayer.playSound("random.successful_hit", 1f,1f);
        }
    }

    @Override
    public void onStompConnected(StompConnectedEvent event) {
        event.getStompInterface().subscribe(StompSubscription.builder()
                .destination("/topic/updates")
                .ackMode(StompSubscription.AckMode.AUTO)
                .stompMessageHandler(this).build());
        event.getStompInterface().subscribe(StompSubscription.builder()
                .destination("/user/queue/messages")
                .ackMode(StompSubscription.AckMode.AUTO)
                .stompMessageHandler(this).build());
    }
}
