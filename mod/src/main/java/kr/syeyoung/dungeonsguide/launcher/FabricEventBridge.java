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

import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.Window;
import kr.syeyoung.dungeonsguide.mod.render.gui.StencilImage;
import kr.syeyoung.dungeonsguide.mod.render.gui.DGGuiRenderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
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
import kr.syeyoung.dungeonsguide.mod.render.world.DGWorldRenderer;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.GLCursors;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
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
        // glfw is not up yet when onInitializeClient runs
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try {
                GLCursors.setupCursors();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

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

        // 1.8.9's RenderWorldLastEvent: handlers record DG world states during extraction, DG's frame graph passes draw them (D123)
        LevelRenderEvents.END_EXTRACTION.register(context -> {
            DGWorldRenderer.beginExtraction(context.camera().position());
            fire(new RenderWorldEvent(context.deltaTracker().getGameTimeDeltaPartialTick(false), context));
        });

        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath("dungeonsguide", "hud_pre"), (graphics, deltaTracker) ->
                fireDrawing(hudPreImage, graphics, context -> new RenderHUDEvent.Pre(deltaTracker.getGameTimeDeltaPartialTick(false), graphics, context)));
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("dungeonsguide", "hud_post"), (graphics, deltaTracker) ->
                fireDrawing(hudPostImage, graphics, context -> new RenderHUDEvent.Post(deltaTracker.getGameTimeDeltaPartialTick(false), graphics, context)));

        // fabric resets per screen events on every init, so they are registered here
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            fire(new ScreenInitEvent.Post(screen, scaledWidth, scaledHeight));

            ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, tickProgress) ->
                    fireDrawing(screenImage, graphics, context -> new ScreenRenderEvent.Post(s, graphics, mouseX, mouseY, tickProgress, context)));

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
    // D126: handlers of drawing events draw into a RenderingContext in gui units, rendered into an image after dispatch
    private static final StencilImage hudPreImage = new StencilImage("Dungeons Guide hud pre event");
    private static final StencilImage hudPostImage = new StencilImage("Dungeons Guide hud post event");
    private static final StencilImage screenImage = new StencilImage("Dungeons Guide screen render event");

    private static boolean fireDrawing(StencilImage image, GuiGraphicsExtractor graphics, Function<RenderingContext, DGEvent> event) {
        Window window = Minecraft.getInstance().getWindow();
        RenderingContext context = new RenderingContext(window.getHeight());
        context.pose().scale(window.getGuiScale(), window.getGuiScale());
        boolean result = fire(event.apply(context));
        if (!context.getStates().isEmpty()) {
            image.ensureSize(window.getWidth(), window.getHeight());
            DGGuiRenderer.INSTANCE.submit(image, context.getStates());
            image.extractComposite(graphics);
        }
        return result;
    }

    public static boolean fire(DGEvent event) {
        if (DungeonsGuide.getDungeonsGuide() == null) return false;
        return DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(event);
    }
}
