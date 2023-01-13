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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;


import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class FeatureUpdateAlarm extends SimpleFeature  {
    public FeatureUpdateAlarm() {
        super("Misc", "Update Alarm","Show a warning in chat when a version has been released.", "etc.updatealarm", true);
    }

    private String stompPayload;

    @DGEventHandler
    public void onTick(DGTickEvent event) {
        if (stompPayload != null) {
            ChatTransmitter.addToQueue(new ChatComponentText(stompPayload));
            stompPayload = null;
            Minecraft.getMinecraft().thePlayer.playSound("random.successful_hit", 1f,1f);
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onStompConnected(StompConnectedEvent event) {

        event.getStompInterface().subscribe("/topic/updates", (stompClient ,payload) -> {
            this.stompPayload = payload;
        });

        event.getStompInterface().subscribe("/user/queue/messages", (stompClient ,payload) -> {
            this.stompPayload = payload;
        });

    }
}
