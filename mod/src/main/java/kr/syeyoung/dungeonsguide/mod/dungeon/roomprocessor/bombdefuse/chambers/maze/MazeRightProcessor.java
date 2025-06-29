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


import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.UBlockState;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MazeRightProcessor extends GeneralDefuseChamberProcessor {
    public MazeRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
        center = chamber.getBlockPos(4,4,4);

        for (int x = 0; x < 9; x++) {
            for (int y =0; y< 6; y++) {
                UBlockState b = chamber.getBlock(x,0,y);
                VectorI3D pos = chamber.getBlockPos(x,0,y);
                blockToBlockPosMap.put(b, pos);
            }
        }
    }

    private final VectorI3D center;
    private final Map<UBlockState, VectorI3D> blockToBlockPosMap = new HashMap<UBlockState, VectorI3D>();
    @Override
    public String getName() {
        return "mazeRight";
    }


    private UBlockState latestRequest = null;

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawTextAtWorld(latestRequest == null ? "Request not received yet" : "Requested received "+latestRequest.getBlock().getLocalizedName() , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        VectorI3D pos = blockToBlockPosMap.get(latestRequest);
        if (pos == null) return;
        RenderUtils.highlightBlock(pos, new Color(0,255,0,100), partialTicks, false);
    }

    @Override
    public void onDataReceive(CompoundBinaryTag compound) {
        if (5 == compound.getByte("a")) {
            String latestRequestid = compound.getString("b");
            latestRequest = ModAPI.getAPI().getBlockRegistry().fromSerializedSeting(latestRequestid);
        }
    }
}
