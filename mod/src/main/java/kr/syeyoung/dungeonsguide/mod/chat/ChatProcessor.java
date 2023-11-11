/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatProcessor {
    public static final ChatProcessor INSTANCE = new ChatProcessor();

    private static final Logger logger = LogManager.getLogger("DG-ChatProcessor");
    private ChatProcessor() {
        Logger l = LogManager.getLogger(GuiNewChat.class);
        if (l instanceof SimpleLogger) {
            ((SimpleLogger) l).setLevel(Level.OFF);
        } else if (l instanceof org.apache.logging.log4j.core.Logger) {
            ((org.apache.logging.log4j.core.Logger) l).setLevel(Level.OFF);
        }
    }

    private Queue<ChatSubscriber> chatSubscriberQueue = new ConcurrentLinkedQueue<>();
    private Queue<Tuple<String, Runnable>> chatQueue = new ConcurrentLinkedQueue<>();


    public void subscribe(ChatSubscriber chatSubscribed) {
        chatSubscriberQueue.add(chatSubscribed);
    }
    public void addToChatQueue(String chat, Runnable onSend, boolean noDupe) {
        if (noDupe && chatQueue.stream().anyMatch(a -> a.getFirst().trim().equalsIgnoreCase(chat.trim()))) return;
        chatQueue.add(new Tuple<>(chat, onSend));
    }


    private long minimumNext = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent clientTickEvent) {
        try {
            if (clientTickEvent.phase == TickEvent.Phase.START && Minecraft.getMinecraft().thePlayer != null && minimumNext < System.currentTimeMillis()) {
                if (!chatQueue.isEmpty()) {
                    Tuple<String, Runnable> tuple = chatQueue.poll();
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(tuple.getFirst());
                    if (tuple.getSecond() != null)
                        tuple.getSecond().run();
                    minimumNext = System.currentTimeMillis() + 700;
                    ChatTransmitter.sendDebugChat(new ChatComponentText("Sending " + tuple.getFirst() + " Secretly"));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onMessage(ClientChatReceivedEvent chatReceivedEvent) {
        if (chatReceivedEvent.type == 2) return;
        String txt = chatReceivedEvent.message.getFormattedText();
        logger.info("[CHAT] {}", txt);

        int processed = 0;
        int listened = 0;
        Map<String, Object> context = new HashMap<>();
        Iterator<ChatSubscriber> it = chatSubscriberQueue.iterator();
        while (it.hasNext()) {
            ChatSubscriber chatSubscribed = it.next();
            context.put("removed", processed);
            context.put("onceListenered", listened);
            try {
                ChatProcessResult chatProcessResult = chatSubscribed.process(txt, context);
                if (chatProcessResult.isRemoveChat()) processed++;
                if (chatProcessResult.isRemoveListener()) listened++;

                if (chatProcessResult.isRemoveChat()) chatReceivedEvent.setResult(Event.Result.DENY);
                if (chatProcessResult.isRemoveListener()) it.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void cancelMessage(ClientChatReceivedEvent chatReceivedEvent) {
        if (chatReceivedEvent.getResult() == Event.Result.DENY)
            chatReceivedEvent.setCanceled(true);
    }
}
