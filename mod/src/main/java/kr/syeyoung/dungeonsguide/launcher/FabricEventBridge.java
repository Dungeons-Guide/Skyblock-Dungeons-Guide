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
import kr.syeyoung.dungeonsguide.mod.events.impl.EntityEnterWorldEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderHUDEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenInitEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenKeyboardEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenMouseEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenRenderEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.WorldLoadEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

// turns fabric callbacks and mixin hooks into dg events
public class FabricEventBridge {
    // fabric calls MODIFY_GAME right after ALLOW_GAME for the same message
    private static ChatReceivedEvent lastGameMessage;

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> fire(new ClientTickEvent(ClientTickEvent.Phase.START)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> fire(new ClientTickEvent(ClientTickEvent.Phase.END)));

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> fire(new WorldLoadEvent(level)));
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> fire(new EntityEnterWorldEvent(entity, level)));

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

        // closest to 1.8.9's RenderWorldLastEvent: end of the main pass
        LevelRenderEvents.END_MAIN.register(context -> {
            fire(new RenderWorldEvent(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), context));
            context.bufferSource().endBatch();
        });

        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath("dungeonsguide", "hud_pre"), (graphics, deltaTracker) ->
                fire(new RenderHUDEvent.Pre(deltaTracker.getGameTimeDeltaPartialTick(false), graphics)));
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("dungeonsguide", "hud_post"), (graphics, deltaTracker) ->
                fire(new RenderHUDEvent.Post(deltaTracker.getGameTimeDeltaPartialTick(false), graphics)));

        // fabric resets per screen events on every init, so they are registered here
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            fire(new ScreenInitEvent.Post(screen, scaledWidth, scaledHeight));

            ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, tickProgress) ->
                    fire(new ScreenRenderEvent.Post(s, graphics, mouseX, mouseY, tickProgress)));

            ScreenMouseEvents.allowMouseClick(screen).register((s, event) ->
                    !fire(new ScreenMouseEvent.Pre(s, ScreenMouseEvent.Action.CLICK, event, event.x(), event.y(), 0, 0, 0, 0)));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, event) ->
                    !fire(new ScreenMouseEvent.Pre(s, ScreenMouseEvent.Action.RELEASE, event, event.x(), event.y(), 0, 0, 0, 0)));
            ScreenMouseEvents.allowMouseDrag(screen).register((s, event, dragX, dragY) ->
                    !fire(new ScreenMouseEvent.Pre(s, ScreenMouseEvent.Action.DRAG, event, event.x(), event.y(), dragX, dragY, 0, 0)));
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, scrollX, scrollY) ->
                    !fire(new ScreenMouseEvent.Pre(s, ScreenMouseEvent.Action.SCROLL, null, mouseX, mouseY, 0, 0, scrollX, scrollY)));

            ScreenKeyboardEvents.allowKeyPress(screen).register((s, event) ->
                    !fire(new ScreenKeyboardEvent.Pre(s, ScreenKeyboardEvent.Action.PRESS, event, null)));
            ScreenKeyboardEvents.afterKeyPress(screen).register((s, event) ->
                    fire(new ScreenKeyboardEvent.Post(s, ScreenKeyboardEvent.Action.PRESS, event, null)));
            ScreenKeyboardEvents.allowKeyRelease(screen).register((s, event) ->
                    !fire(new ScreenKeyboardEvent.Pre(s, ScreenKeyboardEvent.Action.RELEASE, event, null)));
            ScreenKeyboardEvents.afterKeyRelease(screen).register((s, event) ->
                    fire(new ScreenKeyboardEvent.Post(s, ScreenKeyboardEvent.Action.RELEASE, event, null)));
            ScreenKeyboardEvents.allowCharType(screen).register((s, event) ->
                    !fire(new ScreenKeyboardEvent.Pre(s, ScreenKeyboardEvent.Action.CHAR, null, event)));
            ScreenKeyboardEvents.afterCharType(screen).register((s, event) ->
                    fire(new ScreenKeyboardEvent.Post(s, ScreenKeyboardEvent.Action.CHAR, null, event)));
        });
    }

    // minecraft can call into the bridge (mixins) before dungeons guide is created
    public static boolean fire(DGEvent event) {
        if (DungeonsGuide.getDungeonsGuide() == null) return false;
        return DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(event);
    }
}
