package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.stomp.StompInterface;
import kr.syeyoung.dungeonsguide.stomp.StompMessageHandler;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.stomp.StompSubscription;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FeatureBlah extends SimpleFeature implements StompMessageHandler, TickListener {
    public FeatureBlah() {
        super("ETC", "TEST","test.test");
        e.getDungeonsGuide().getStompConnection().subscribe(StompSubscription.builder()
                .destination("/topic/updates")
                .ackMode(StompSubscription.AckMode.AUTO)
                .stompMessageHandler(this).build());
    }

    Queue<StompPayload> stompPayloadQueue = new ConcurrentLinkedQueue<StompPayload>();
    @Override
    public void handle(StompInterface stompInterface, StompPayload stompPayload) {
        stompPayloadQueue.add(stompPayload);
    }


    @Override
    public void onTick() {
        while (!stompPayloadQueue.isEmpty()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(stompPayloadQueue.poll().payload()));
        }
    }
}
