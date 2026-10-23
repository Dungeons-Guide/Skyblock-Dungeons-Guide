/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.events.impl.ClientTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import kr.syeyoung.dungeonsguide.mod.events.impl.ChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.events.annotations.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatTransmitter {

    public static final String PREFIX = "§eDungeons Guide §7:: ";
    public static String prefix = "§eDungeons Guide §7:: ";


    public static ChatTransmitter INSTANCE = new ChatTransmitter();

    @Getter
    static Queue<MutableComponent> receiveQueue = new ConcurrentLinkedQueue<>();

    public static void addToQueue(String chat, boolean noDupe) {
        addToQueue(Component.literal(chat), noDupe);
    }

    public static void addToQueue(MutableComponent chat) {
        addToQueue(chat, false);
    }
    public static void addToQueue(MutableComponent chat, boolean noDupe) {
        if(noDupe && receiveQueue.stream().anyMatch(a -> a.equals(chat))) return;
        receiveQueue.add(chat);
    }

    public static void addToQueue(String s) {
        addToQueue(s, false);
    }

    public static void sendDebugChat(Component iChatComponent) {
        if(FeatureRegistry.DEBUG == null) return;
        if (FeatureRegistry.DEBUG.isEnabled())
            addToQueue((MutableComponent) iChatComponent);
    }

    public static void sendDebugChat(String text) {
        sendDebugChat(Component.literal(text));
    }


    @SubscribeEvent
    public void onTick(ClientTickEvent clientTickEvent) {
        if(clientTickEvent.phase != ClientTickEvent.Phase.START && Minecraft.getInstance().player == null) return;

        while (!receiveQueue.isEmpty() && Minecraft.getInstance().player != null) {
            ChatReceivedEvent event = new ChatReceivedEvent((byte) 1, receiveQueue.poll());
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(event);
            if (!event.isCanceled()) {
                Minecraft.getInstance().gui.getChat().addClientSystemMessage(event.message);
            }
        }
    }


}
