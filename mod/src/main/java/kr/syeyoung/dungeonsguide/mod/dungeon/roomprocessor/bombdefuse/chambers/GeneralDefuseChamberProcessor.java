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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers;

import kr.syeyoung.dungeonsguide.mod.events.impl.RenderHUDEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.LivingTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.LivingDeathEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenRenderEvent;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEvent;
import com.mojang.blaze3d.platform.InputConstants;

@Getter
public abstract class GeneralDefuseChamberProcessor  implements ChamberProcessor{
    private final RoomProcessorBombDefuseSolver solver;
    private final BDChamber chamber;

    public GeneralDefuseChamberProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        this.solver = solver;
        this.chamber = chamber;
    }


    @Override
    public void onDataReceive(CompoundTag compound) {

    }

    @Override
    public void tick() {

    }

    @Override
    public void drawScreen(RenderHUDEvent event) {

    }

    @Override
    public void drawWorld(RenderWorldEvent event) {

    }

    @Override
    public void chatReceived(Component chat) {

    }

    @Override
    public void onEntityDeath(LivingDeathEvent deathEvent) {

    }

    @Override
    public void actionbarReceived(Component chat) {

    }
    @Override
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {

    }

    @Override
    public boolean readGlobalChat() {
        return false;
    }

    @Override
    public void onPostGuiRender(ScreenRenderEvent.Post event) {

    }

    @Override
    public void onEntityUpdate(LivingTickEvent updateEvent) {

    }

    protected void drawPressKey(RenderHUDEvent event) {
        Font fr = Minecraft.getInstance().font;
        String str = "Press "+ FeatureRegistry.SOLVER_BOMBDEFUSE.<InputConstants.Key>getParameter("key").getValue().getDisplayName().getString() + " to save and send solution";
        event.graphics.text(fr, str, (event.graphics.guiWidth() - fr.width(str)) / 2, (event.graphics.guiHeight() - fr.lineHeight) / 2, 0xFFFFFFFF, false);
    }

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {
        if (keyInputEvent.getKey().equals(FeatureRegistry.SOLVER_BOMBDEFUSE.<InputConstants.Key>getParameter("key").getValue())) {
            if (!getChamber().isWithinAbsolute(Minecraft.getInstance().player.blockPosition())) {
                return;
            }
            onSendData();
        }
    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {

    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {

    }

    @Override
    public void chunkUpdate(int chunkX, int chunkZ) {

    }

    public void onSendData() {}
}
