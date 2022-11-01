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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.creeper;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import java.awt.*;

public class CreeperRightProcessor extends GeneralDefuseChamberProcessor {
    public CreeperRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        poses = new BlockPos[9];
        for (int i = 0; i < 9; i++) {
            poses[i] = chamber.getBlockPos(3+(i%3), 1, 1+(i/3));
        }
        center = chamber.getBlockPos(4,4,4);
    }

    @Override
    public String getName() {
        return "creeperRight";
    }


    private int answer = -1;
    private final BlockPos[] poses;
    private final BlockPos center;
    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawTextAtWorld(answer == -1 ? "Answer not received yet. Visit left room to obtain solution" : "" , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        if (answer != -1) {
            for (int i = 0; i < 9; i++) {
                if (((answer >> i) & 0x01) == 0) {
                    RenderUtils.highlightBlock(poses[i], new Color(0,255,0, 50), partialTicks, false);
                }
            }
        }
    }
    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if (2 == compound.getByte("a")) {
            answer = compound.getInteger("b");
        }
    }
}
