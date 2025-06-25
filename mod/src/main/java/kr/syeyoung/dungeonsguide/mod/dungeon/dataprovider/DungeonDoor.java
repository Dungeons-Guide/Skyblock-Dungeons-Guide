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

package kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider;

import com.google.common.collect.Sets;
import kr.syeyoung.modapi.data.VectorI3D;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

@Getter
public class DungeonDoor {
    private final World w;
    private final VectorI3D position;
    private final EDungeonDoorType type;
    private boolean isZDir;

    private static final Set<Block> legalBlocks = Sets.newHashSet(Blocks.coal_block, Blocks.barrier, Blocks.monster_egg, Blocks.air, Blocks.stained_hardened_clay);

    public DungeonDoor(World world, VectorI3D pos, EDungeonDoorType type) {
        this.w = world;
        this.position = pos;
        Block itShouldBeAll = world.getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock();

        if (type == EDungeonDoorType.WITHER && itShouldBeAll == Blocks.air) type = EDungeonDoorType.WITHER_FAIRY;
        this.type = type;
        boolean exist = type.isExist();

        for (int x = -1; x<=1; x++) {
            for (int y = -1; y<=1; y++) {
                for (int z = -1; z<=1; z++) {
                    VectorI3D pos2 = pos.add(x,y,z);
                    Block block = world.getChunkFromBlockCoords(new BlockPos(pos2.x, pos2.y, pos2.z)).getBlock(new BlockPos(pos2.x, pos2.y, pos2.z));
                    if (itShouldBeAll != block) exist = false;
                }
            }
        }
        if (exist) {
            VectorI3D ZCheck = pos.add(0,0,2);
            isZDir = world.getChunkFromBlockCoords(new BlockPos(ZCheck.x, ZCheck.y, ZCheck.z)).getBlock(new BlockPos(ZCheck.x, ZCheck.y, ZCheck.z)) == Blocks.air;

            if (isZDir) {
                for (int x = -1; x<=1; x++) {
                    for (int y = -1; y<=1; y++) {
                        for (int z = -2; z<=2; z+=4) {
                            VectorI3D pos2 = pos.add(x,y,z);
                            Block block = world.getChunkFromBlockCoords(new BlockPos(pos2.x, pos2.y, pos2.z)).getBlock(new BlockPos(pos2.x, pos2.y, pos2.z));
                            if (block != Blocks.air) exist = false;
                        }
                    }
                }
            } else {
                for (int x = -2; x<=2; x+=4) {
                    for (int y = -1; y<=1; y++) {
                        for (int z = -1; z<=1; z++) {
                            VectorI3D pos2 = pos.add(x,y,z);
                            Block block = world.getChunkFromBlockCoords(new BlockPos(pos2.x, pos2.y, pos2.z)).getBlock(new BlockPos(pos2.x, pos2.y, pos2.z));
                            if (block != Blocks.air) exist = false;
                        }
                    }
                }
            }
        }
        if (!exist) {
            isZDir = false;
        }
    }
}
