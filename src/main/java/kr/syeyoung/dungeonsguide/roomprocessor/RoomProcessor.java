/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.events.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.events.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.events.PlayerInteractEntityEvent;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public interface RoomProcessor {
    void tick();
    void drawScreen(float partialTicks);
    void drawWorld(float partialTicks);
    void chatReceived(IChatComponent chat);
    void actionbarReceived(IChatComponent chat);

    boolean readGlobalChat();

    void onPostGuiRender(GuiScreenEvent.DrawScreenEvent.Post event);
    void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent);
    void onEntityDeath(LivingDeathEvent deathEvent);

    void onKeybindPress(KeyBindPressedEvent keyInputEvent);

    void onInteract(PlayerInteractEntityEvent event);
    void onInteractBlock(PlayerInteractEvent event);

    void onBlockUpdate(BlockUpdateEvent blockUpdateEvent);
}