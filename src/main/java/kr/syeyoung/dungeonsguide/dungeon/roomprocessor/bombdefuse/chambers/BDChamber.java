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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.chambers;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
@Data
public class BDChamber {
    private DungeonRoom room;
    private OffsetPointSet chamberBlocks;

    private boolean isLeft;
    private int level;

    private ChamberProcessor processor;

    // for whatever's sake, 6: z, 9: x. Starts from botoom right, left, then up

    public OffsetPoint getOffsetPoint(int x, int z) {
        return chamberBlocks.getOffsetPointList().get(z * 9 + x);
    }

    public BlockPos getBlockPos(int x, int y, int z) {
        return getOffsetPoint(x,z).getBlockPos(room).add(0,y,0);
    }

    public IBlockState getBlock(int x, int y, int z) {
        BlockPos pos = getBlockPos(x,y,z);
        return room.getContext().getWorld().getBlockState(pos);
    }

    public boolean isWithinAbsolute(int x, int y, int z) {
        return isWithinAbsolute(new BlockPos(x,68,z));
    }
    public boolean isWithinAbsolute(BlockPos pos) {
        return chamberBlocks.getOffsetPointList().contains(new OffsetPoint(room, new BlockPos(pos.getX(), 68, pos.getZ())));
    }


    public <T extends Entity> T getEntityAt(Class<T> entity, int x, int y, int z) {
        final BlockPos pos = getBlockPos(x,y,z);
        return getEntityAt(entity, pos);
    }
    public <T extends Entity> T getEntityAt(Class<T> entity, final BlockPos pos) {
        List<T> entities = room.getContext().getWorld().getEntities(entity, new Predicate<T>() {
            @Override
            public boolean apply(@Nullable T input) {
                return input.getPosition().equals(pos);
            }
        });
        if (entities.size() == 0) return null;
        return entities.get(0);
    }
}
