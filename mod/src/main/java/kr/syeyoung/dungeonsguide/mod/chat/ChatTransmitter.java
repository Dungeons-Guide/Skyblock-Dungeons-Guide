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

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Getter;
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
    public static String prefix = "§eDungeons Guide §7:: ";


    public static ChatTransmitter INSTANCE = new ChatTransmitter();

    @Getter
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

        while (!receiveQueue.isEmpty() && Minecraft.getMinecraft().thePlayer != null) {
            ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, receiveQueue.poll());
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(event.message);
            }
        }
    }


}
