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
import lombok.Getter;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DGWorld;

import java.util.Set;

@Getter
public class DungeonDoor {
    private final DGWorld w;
    private final BlockPos position;
    private final EDungeonDoorType type;
    private boolean isZDir;

    private static final Set<LegacyBlock> legalBlocks = Sets.newHashSet(LegacyBlock.COAL_BLOCK, LegacyBlock.BARRIER, LegacyBlock.MONSTER_EGG, LegacyBlock.AIR, LegacyBlock.STAINED_HARDENED_CLAY);

    public DungeonDoor(DGWorld world, BlockPos pos, EDungeonDoorType type) {
        this.w = world;
        this.position = pos;
        BlockState itShouldBeAll = world.getBlockState(pos);

        if (type == EDungeonDoorType.WITHER && itShouldBeAll.isAir()) type = EDungeonDoorType.WITHER_FAIRY;
        this.type = type;
        boolean exist = type.isExist();

        for (int x = -1; x<=1; x++) {
            for (int y = -1; y<=1; y++) {
                for (int z = -1; z<=1; z++) {
                    BlockPos pos2 = pos.offset(x,y,z);
                    BlockState block = world.getBlockState(pos2);
                    // 1.8.9 compared blocks without meta
                    if (LegacyBlock.legacyId(itShouldBeAll) != LegacyBlock.legacyId(block)) exist = false;
                }
            }
        }
        if (exist) {
            BlockPos ZCheck = pos.offset(0,0,2);
            isZDir = world.isAir(ZCheck);

            if (isZDir) {
                for (int x = -1; x<=1; x++) {
                    for (int y = -1; y<=1; y++) {
                        for (int z = -2; z<=2; z+=4) {
                            BlockPos pos2 = pos.offset(x,y,z);
                            if (!world.isAir(pos2)) exist = false;
                        }
                    }
                }
            } else {
                for (int x = -2; x<=2; x+=4) {
                    for (int y = -1; y<=1; y++) {
                        for (int z = -1; z<=1; z++) {
                            BlockPos pos2 = pos.offset(x,y,z);
                            if (!world.isAir(pos2)) exist = false;
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
