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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.maze;

import kr.syeyoung.dungeonsguide.mod.events.impl.RenderHUDEvent;
import net.minecraft.world.level.block.state.BlockState;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.mojang.blaze3d.platform.InputConstants;

public class MazeLeftProcessor extends GeneralDefuseChamberProcessor {
    public MazeLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
    }

    @Override
    public String getName() {
        return "mazeLeft";
    }


    @Override
    public void drawScreen(RenderHUDEvent event) {
        if (Minecraft.getInstance().hitResult == null ) return;
        HitResult pos = Minecraft.getInstance().hitResult;
        if (pos.getType() != HitResult.Type.BLOCK) return;

        BlockState b = DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(((BlockHitResult) pos).getBlockPos());

        Font fr = Minecraft.getInstance().font;
        String str = "Press "+ FeatureRegistry.SOLVER_BOMBDEFUSE.<InputConstants.Key>getParameter("key").getValue().getDisplayName().getString() + " to request open "+b.getBlock().getName().getString();
        event.context.drawString(str, (Minecraft.getInstance().getWindow().getGuiScaledWidth() - fr.width(str)) / 2, (Minecraft.getInstance().getWindow().getGuiScaledHeight() - fr.lineHeight) / 2, 0xFFFFFFFF, false);
    }

    @Override
    public void onSendData() {
        if (Minecraft.getInstance().hitResult == null ) return;
        HitResult pos = Minecraft.getInstance().hitResult;
        if (pos.getType() != HitResult.Type.BLOCK) return;

        BlockPos block = ((BlockHitResult) pos).getBlockPos();
        BlockState b = getChamber().getRoom().getContext().getWorld().getBlockState(block);

        CompoundTag nbt = new CompoundTag();
        nbt.putByte("a", (byte) 5);
        nbt.putInt("b", LegacyBlock.legacyId(b));
        getSolver().communicate(nbt);
    }
}
