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
import kr.syeyoung.dungeonsguide.mod.events.impl.ItemTooltipEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RegisterCommandEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderHUDEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenInitEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenKeyboardEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenMouseEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenRenderEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.WorldLoadEvent;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

// turns fabric callbacks and mixin hooks into dg events
public class FabricEventBridge {
    // fabric calls MODIFY_GAME right after ALLOW_GAME for the same message
    private static ChatReceivedEvent lastGameMessage;

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> fire(new ClientTickEvent(ClientTickEvent.Phase.START)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> fire(new ClientTickEvent(ClientTickEvent.Phase.END)));

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> fire(new WorldLoadEvent(level)));
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> fire(new EntityEnterWorldEvent(entity, level)));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) ->
                fire(new RegisterCommandEvent(dispatcher, buildContext)));

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) ->
                fire(new ItemTooltipEvent(stack, lines, tooltipFlag.isAdvanced())));

        // these also run on the integrated server, and per hand. 1.8.9 only had the client main hand
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            return fire(new PlayerInteractEvent(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, hitResult.getBlockPos(), hitResult.getDirection(), level, player))
                    ? InteractionResult.FAIL : InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            return fire(new PlayerInteractEvent(PlayerInteractEvent.Action.RIGHT_CLICK_AIR, null, null, level, player))
                    ? InteractionResult.FAIL : InteractionResult.PASS;
        });
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (!level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            return fire(new PlayerInteractEvent(PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, pos, direction, level, player))
                    ? InteractionResult.FAIL : InteractionResult.PASS;
        });

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
                    !fire(new ScreenMouseEvent.Click(s, event)));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, event) ->
                    !fire(new ScreenMouseEvent.Release(s, event)));
            ScreenMouseEvents.allowMouseDrag(screen).register((s, event, dragX, dragY) ->
                    !fire(new ScreenMouseEvent.Drag(s, event, dragX, dragY)));
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, scrollX, scrollY) ->
                    !fire(new ScreenMouseEvent.Scroll(s, mouseX, mouseY, scrollX, scrollY)));

            ScreenKeyboardEvents.allowKeyPress(screen).register((s, event) ->
                    !fire(new ScreenKeyboardEvent.Press.Pre(s, event)));
            ScreenKeyboardEvents.afterKeyPress(screen).register((s, event) ->
                    fire(new ScreenKeyboardEvent.Press.Post(s, event)));
            ScreenKeyboardEvents.allowKeyRelease(screen).register((s, event) ->
                    !fire(new ScreenKeyboardEvent.Release.Pre(s, event)));
            ScreenKeyboardEvents.afterKeyRelease(screen).register((s, event) ->
                    fire(new ScreenKeyboardEvent.Release.Post(s, event)));
            ScreenKeyboardEvents.allowCharType(screen).register((s, event) ->
                    !fire(new ScreenKeyboardEvent.Char.Pre(s, event)));
            ScreenKeyboardEvents.afterCharType(screen).register((s, event) ->
                    fire(new ScreenKeyboardEvent.Char.Post(s, event)));
        });
    }

    // minecraft can call into the bridge (mixins) before dungeons guide is created
    public static boolean fire(DGEvent event) {
        if (DungeonsGuide.getDungeonsGuide() == null) return false;
        return DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(event);
    }
}
