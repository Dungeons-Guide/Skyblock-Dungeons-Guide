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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.mod.events.impl.DGChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;

public interface RoomProcessor {
    void tick();
    void drawScreen(float partialTicks, RenderingContext context);
    void drawWorld(UWorldRenderContext context, float partialTicks);
    void chatReceived(DGChatReceivedEvent chat);
    void actionbarReceived(ActionBarReceivedEvent chat);

    boolean readGlobalChat();

    void onEntityUpdate(LivingEntityTickEvent updateEvent);
    void onEntityDeath(LivingEntityDeathEvent deathEvent);

    void onKeybindPress(KeyBindPressedEvent keyInputEvent);

    void onInteract(PlayerInteractEntityEvent event);
    void onInteractBlock(PlayerInteractEvent event);

    void onBlockUpdate(BlockUpdateEvent blockUpdateEvent);

    void chunkUpdate(int chunkX, int chunkZ);
}