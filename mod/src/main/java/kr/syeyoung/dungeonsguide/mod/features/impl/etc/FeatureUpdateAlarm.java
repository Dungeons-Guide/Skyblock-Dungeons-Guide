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
import kr.syeyoung.dungeonsguide.mod.events.impl.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.event.events.ClientTickEvent;

public class FeatureUpdateAlarm extends SimpleFeature  {
    public FeatureUpdateAlarm() {
        super("Misc", "Update Alarm","Show a warning in chat when a version has been released.", "etc.updatealarm", true);
    }

    private String stompPayload;

    @DGEventHandler
    public void onTick(ClientTickEvent event) {
        if (stompPayload != null) {
            ChatTransmitter.addToQueue(stompPayload);
            stompPayload = null;
            ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("random.successful_hit"), 1);
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
