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

import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import net.minecraft.world.level.block.state.BlockState;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MazeRightProcessor extends GeneralDefuseChamberProcessor {
    public MazeRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
        center = chamber.getBlockPos(4,4,4);

        for (int x = 0; x < 9; x++) {
            for (int y =0; y< 6; y++) {
                BlockState b = chamber.getBlock(x,0,y);
                BlockPos pos = chamber.getBlockPos(x,0,y);
                // 1.8.9 keyed by block, without meta
                blockToBlockPosMap.put(LegacyBlock.legacyId(b), pos);
            }
        }
    }

    private final BlockPos center;
    private final Map<Integer, BlockPos> blockToBlockPosMap = new HashMap<Integer, BlockPos>();
    @Override
    public String getName() {
        return "mazeRight";
    }


    private BlockState latestRequest = null;

    @Override
    public void drawWorld(RenderWorldEvent event) {
        super.drawWorld(event);
        RenderUtils.drawTextAtWorld(latestRequest == null ? "Request not received yet" : "Requested received "+latestRequest.getBlock().getName().getString() , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, event.partialTicks);
        BlockPos pos = latestRequest == null ? null : blockToBlockPosMap.get(LegacyBlock.legacyId(latestRequest));
        if (pos == null) return;
        RenderUtils.highlightBlock(pos, new Color(0,255,0,100), event.partialTicks, false);
    }

    @Override
    public void onDataReceive(CompoundTag compound) {
        if (5 == compound.getByteOr("a", (byte) 0)) {
            int latestRequestid = compound.getIntOr("b", 0);
            latestRequest = LegacyBlock.state(latestRequestid, 0);
        }
    }
}
