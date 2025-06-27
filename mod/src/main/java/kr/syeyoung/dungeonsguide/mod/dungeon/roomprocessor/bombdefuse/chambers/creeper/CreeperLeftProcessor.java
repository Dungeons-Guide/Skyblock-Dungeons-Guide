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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.creeper;


import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.BlockType;
import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;

public class CreeperLeftProcessor extends GeneralDefuseChamberProcessor {
    public CreeperLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        poses = new VectorI3D[9];
        for (int i = 0; i < 9; i++) {
            poses[i] = chamber.getBlockPos(3+(i%3), 1, 1+(i/3));
        }
    }

    @Override
    public String getName() {
        return "creeperLeft";
    }


    private int answer = -1;
    private final VectorI3D[] poses;
    @Override
    public void tick() {
        super.tick();
        if (answer != -1) return;
        answer = 0;
        for (int i = 0; i < poses.length; i++) {
            VectorI3D pos = poses[i];
            if (getChamber().getRoom().getContext().getUworld().getBlockStateAt(pos).isOf(BlockType.AIR)) {
                answer |= (1 << i);
            }
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (answer == -1) return;
        drawPressKey();
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        for (int i = 0; i < 9; i++) {
            if (((answer >> i) & 0x01) != 0) {
                RenderUtils.highlightBlock(poses[i], new Color(255,0,0,100), partialTicks, false);
            } else {
                RenderUtils.highlightBlock(poses[i], new Color(0,255,0,100), partialTicks, false);
            }
        }
    }

    @Override
    public void onSendData() {
        if (answer == -1) return;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("a", (byte) 2);
        nbt.setInteger("b", answer);
        getSolver().communicate(nbt);
    }

    @Override
    public void onDataReceive(NBTTagCompound compound) {
        if (2 == compound.getByte("a")) {
            answer = compound.getInteger("b");
        }
    }
}
