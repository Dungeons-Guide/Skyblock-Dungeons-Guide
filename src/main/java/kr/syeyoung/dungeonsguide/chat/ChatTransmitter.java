package kr.syeyoung.dungeonsguide.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatTransmitter {

    public ChatTransmitter() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    static Queue<ChatComponentText> receiveQueue = new ConcurrentLinkedQueue<>();

    public static void addToReciveChatQueue(String chat, boolean noDupe) {
        addToReciveChatQueue(new ChatComponentText(chat), noDupe);
    }

    public static void addToReciveChatQueue(ChatComponentText chat) {
        addToReciveChatQueue(chat, false);
    }
    public static void addToReciveChatQueue(ChatComponentText chat, boolean noDupe) {
        if(noDupe && receiveQueue.stream().anyMatch(a -> a.equals(chat))) return;
        receiveQueue.add(chat);
    }

    public static void addToReciveChatQueue(String s) {
        addToReciveChatQueue(s, false);
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
