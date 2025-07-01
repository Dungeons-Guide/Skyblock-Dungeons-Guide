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

import kr.syeyoung.dungeonsguide.mod.events.impl.DGChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.event.ListenerPriority;
import kr.syeyoung.modapi.event.SubscribeEvent;
import kr.syeyoung.modapi.event.events.ChatReceivedEvent;
import kr.syeyoung.modapi.event.events.ClientTickEvent;
import net.kyori.adventure.text.Component;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;

import java.util.*;
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

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onTick(ClientTickEvent clientTickEvent) {
        try {
            if (ModAPI.getAPI().getPlayer() != null && minimumNext < System.currentTimeMillis()) {
                if (!chatQueue.isEmpty()) {
                    Tuple<String, Runnable> tuple = chatQueue.poll();
                    ModAPI.getAPI().getPlayer().sendMessageToServer(tuple.getFirst());
                    if (tuple.getSecond() != null)
                        tuple.getSecond().run();
                    minimumNext = System.currentTimeMillis() + 700;
                    ChatTransmitter.sendDebugChat("Sending " + tuple.getFirst() + " Secretly");
                }
            }
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }


    private ThreadLocal<Stack<Boolean>> chatEventLocal = ThreadLocal.withInitial(Stack::new);

    @SubscribeEvent(priority = ListenerPriority.FIRST, receiveCanceled = true)
    public void onEvent(ChatReceivedEvent event) {
        String txt = TextUtils.getNearestFormattedText(event.chat);
        logger.info("[CHAT] {}", txt);

        int processed = 0;
        int listened = 0;
        Map<String, Object> context = new HashMap<>();
        Iterator<ChatSubscriber> it = chatSubscriberQueue.iterator();
        boolean result = event.isCanceled();
        while (it.hasNext()) {
            ChatSubscriber chatSubscribed = it.next();
            context.put("removed", processed);
            context.put("onceListenered", listened);
            try {
                ChatProcessResult chatProcessResult = chatSubscribed.process(txt, context);
                if (chatProcessResult.isRemoveChat()) processed++;
                if (chatProcessResult.isRemoveListener()) listened++;

                if (chatProcessResult.isRemoveChat()) result = true;
                if (chatProcessResult.isRemoveListener()) it.remove();
            } catch (Exception e) {
                FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
            }
        }
        chatEventLocal.get().push(result);
    }

    @SubscribeEvent(priority = ListenerPriority.LAST, receiveCanceled = true)
    public void cancelMessage(DGChatReceivedEvent chatReceivedEvent) {
        if (chatEventLocal.get().pop()) {
            chatReceivedEvent.setCanceled(true);
        }
    }



    private ThreadLocal<Stack<Component>> origin = ThreadLocal.withInitial(() -> new Stack<>());
    @kr.syeyoung.modapi.event.SubscribeEvent(receiveCanceled = true, priority = ListenerPriority.FIRST)
    public void onChatReceived1(ChatReceivedEvent event) {
        System.out.println(event.chat);

        origin.get().push(event.chat);
        DGChatReceivedEvent dgChatReceivedEvent = new DGChatReceivedEvent(
                TextUtils.getNearestFormattedText(event.chat),
                origin.get().peek(),
                event.chat,
                event.isCanceled()
        );
        ModAPI.getAPI().getEventBus().fireEvent(dgChatReceivedEvent, ListenerPriority.FIRST);
        event.chat = dgChatReceivedEvent.getChat();
        event.setCanceled(dgChatReceivedEvent.isCanceled());
    }
    @kr.syeyoung.modapi.event.SubscribeEvent(receiveCanceled = true, priority = ListenerPriority.SECOND)
    public void onChatReceived2(ChatReceivedEvent event) {
        DGChatReceivedEvent dgChatReceivedEvent = new DGChatReceivedEvent(
                TextUtils.getNearestFormattedText(event.chat),
                origin.get().peek(),
                event.chat,
                event.isCanceled()
        );
        ModAPI.getAPI().getEventBus().fireEvent(dgChatReceivedEvent, ListenerPriority.SECOND);
        event.chat = dgChatReceivedEvent.getChat();
        event.setCanceled(dgChatReceivedEvent.isCanceled());
    }
    @kr.syeyoung.modapi.event.SubscribeEvent(receiveCanceled = true, priority = ListenerPriority.THIRD)
    public void onChatReceived3(ChatReceivedEvent event) {
        DGChatReceivedEvent dgChatReceivedEvent = new DGChatReceivedEvent(
                TextUtils.getNearestFormattedText(event.chat),
                origin.get().peek(),
                event.chat,
                event.isCanceled()
        );
        ModAPI.getAPI().getEventBus().fireEvent(dgChatReceivedEvent, ListenerPriority.THIRD);
        event.chat = dgChatReceivedEvent.getChat();
        event.setCanceled(dgChatReceivedEvent.isCanceled());
    }
    @kr.syeyoung.modapi.event.SubscribeEvent(receiveCanceled = true, priority = ListenerPriority.FOURTH)
    public void onChatReceived4(ChatReceivedEvent event) {
        DGChatReceivedEvent dgChatReceivedEvent = new DGChatReceivedEvent(
                TextUtils.getNearestFormattedText(event.chat),
                origin.get().peek(),
                event.chat,
                event.isCanceled()
        );
        ModAPI.getAPI().getEventBus().fireEvent(dgChatReceivedEvent, ListenerPriority.FOURTH);
        event.chat = dgChatReceivedEvent.getChat();
        event.setCanceled(dgChatReceivedEvent.isCanceled());
    }
    @kr.syeyoung.modapi.event.SubscribeEvent(receiveCanceled = true, priority = ListenerPriority.LAST)
    public void onChatReceived5(ChatReceivedEvent event) {
        DGChatReceivedEvent dgChatReceivedEvent = new DGChatReceivedEvent(
                TextUtils.getNearestFormattedText(event.chat),
                origin.get().peek(),
                event.chat,
                event.isCanceled()
        );
        ModAPI.getAPI().getEventBus().fireEvent(dgChatReceivedEvent, ListenerPriority.LAST);
        event.chat = dgChatReceivedEvent.getChat();
        event.setCanceled(dgChatReceivedEvent.isCanceled());
        origin.get().pop();
    }
}
