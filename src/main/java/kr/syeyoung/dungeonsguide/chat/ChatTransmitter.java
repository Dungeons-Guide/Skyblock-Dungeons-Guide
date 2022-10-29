package kr.syeyoung.dungeonsguide.chat;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatTransmitter {

    public static final String PREFIX = "§eDungeons Guide §7:: ";

    public ChatTransmitter() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    static Queue<ChatComponentText> receiveQueue = new ConcurrentLinkedQueue<>();

    public static void addToQueue(String chat, boolean noDupe) {
        addToQueue(new ChatComponentText(chat), noDupe);
    }

    public static void addToQueue(ChatComponentText chat) {
        addToQueue(chat, false);
    }
    public static void addToQueue(ChatComponentText chat, boolean noDupe) {
        if(noDupe && receiveQueue.stream().anyMatch(a -> a.equals(chat))) return;
        receiveQueue.add(chat);
    }

    public static void addToQueue(String s) {
        addToQueue(s, false);
    }

    public static void sendDebugChat(IChatComponent iChatComponent) {
        if(FeatureRegistry.DEBUG == null) return;
        if (FeatureRegistry.DEBUG.isEnabled())
            addToQueue((ChatComponentText) iChatComponent);
    }

    public static void sendDebugChat(String text) {
        sendDebugChat(new ChatComponentText(text));
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent clientTickEvent) {
        if(clientTickEvent.phase != TickEvent.Phase.START && Minecraft.getMinecraft().thePlayer == null) return;

        if (!receiveQueue.isEmpty()) {
            ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, receiveQueue.poll());
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(event.message);
            }
        }


    }


}
