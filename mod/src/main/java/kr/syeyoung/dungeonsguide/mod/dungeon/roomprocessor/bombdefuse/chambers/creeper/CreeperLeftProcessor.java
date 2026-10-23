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

import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

import java.awt.*;

public class CreeperLeftProcessor extends GeneralDefuseChamberProcessor {
    public CreeperLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        poses = new BlockPos[9];
        for (int i = 0; i < 9; i++) {
            poses[i] = chamber.getBlockPos(3+(i%3), 1, 1+(i/3));
        }
    }

    @Override
    public String getName() {
        return "creeperLeft";
    }


    private int answer = -1;
    private final BlockPos[] poses;
    @Override
    public void tick() {
        super.tick();
        if (answer != -1) return;
        answer = 0;
        for (int i = 0; i < poses.length; i++) {
            BlockPos pos = poses[i];
            if (LegacyBlock.AIR.is(getChamber().getRoom().getContext().getWorld().getBlockState(pos))) {
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
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("a", (byte) 2);
        nbt.putInt("b", answer);
        getSolver().communicate(nbt);
    }

    @Override
    public void onDataReceive(CompoundTag compound) {
        if (2 == compound.getByteOr("a", (byte) 0)) {
            answer = compound.getIntOr("b", 0);
        }
    }
}
