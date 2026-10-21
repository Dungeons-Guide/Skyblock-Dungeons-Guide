/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.events.DGEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ClientTickEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

// turns fabric callbacks and mixin hooks into dg events
public class FabricEventBridge {
    // fabric calls MODIFY_GAME right after ALLOW_GAME for the same message
    private static ChatReceivedEvent lastGameMessage;

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> fire(new ClientTickEvent(ClientTickEvent.Phase.START)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> fire(new ClientTickEvent(ClientTickEvent.Phase.END)));

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            lastGameMessage = new ChatReceivedEvent(overlay ? (byte) 2 : (byte) 1, message);
            return !fire(lastGameMessage);
        });
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            ChatReceivedEvent event = lastGameMessage;
            lastGameMessage = null;
            return event == null ? message : event.message;
        });
        // TODO: there is no MODIFY for player chat on 26.1, so replacing the message does nothing here
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, playerChatMessage, sender, boundChatType, timestamp) ->
                !fire(new ChatReceivedEvent((byte) 0, message)));
    }

    public static boolean fire(DGEvent event) {
        return DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(event);
    }
}
