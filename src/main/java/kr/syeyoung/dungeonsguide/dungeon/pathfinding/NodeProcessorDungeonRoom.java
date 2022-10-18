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

package kr.syeyoung.dungeonsguide.dungeon.pathfinding;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.pathfinder.NodeProcessor;

import java.util.Set;

public class NodeProcessorDungeonRoom extends NodeProcessor {
    private final DungeonRoom dungeonRoom;
    private final BlockPos sub;

    public NodeProcessorDungeonRoom(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
        sub = dungeonRoom.getMax().subtract(dungeonRoom.getMin());
    }

    @Override
    public PathPoint getPathPointTo(Entity entityIn) {
        return openPoint((int) entityIn.posX - dungeonRoom.getMin().getX(), (int) entityIn.posY - dungeonRoom.getMin().getY(),
                (int) entityIn.posZ - dungeonRoom.getMin().getZ());
    }

    @Override
    public PathPoint getPathPointToCoords(Entity entityIn, double x, double y, double z) {
        return openPoint((int) x - dungeonRoom.getMin().getX(), (int) y - dungeonRoom.getMin().getY(),
                (int) z - dungeonRoom.getMin().getZ());
    }

    private static final EnumFacing[] values2 = new EnumFacing[] {
        EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.UP
    };



    @Override
    public int findPathOptions(PathPoint[] pathOptions, Entity entityIn, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {

        int i = 0;
        for (EnumFacing ef : values2) {
            Vec3i dir = ef.getDirectionVec();
            int newX = currentPoint.xCoord + dir.getX();
            int newY = currentPoint.yCoord + dir.getY();
            int newZ = currentPoint.zCoord + dir.getZ();

            if (newX < 0 || newZ < 0) continue;
            if (newX > sub.getX() || newZ > sub.getZ()) continue;

            BlockPos add1 = dungeonRoom.getMin().add(newX, newY, newZ);
            World playerWorld = entityIn.getEntityWorld();

            IBlockState curr = playerWorld.getBlockState(add1);


            IBlockState up = playerWorld.getBlockState(dungeonRoom.getMin().add(newX, newY + 1, newZ));

            if (isValidBlock(curr) && isValidBlock(up)) {
                PathPoint pt = openPoint(newX, newY, newZ);
                if (pt.visited) continue;
                pathOptions[i++] = pt;
                continue;
            }

            if (curr.getBlock() == Blocks.air) {
                if (up.getBlock() == Blocks.stone_slab
                        || up.getBlock() == Blocks.wooden_slab
                        || up.getBlock() == Blocks.stone_slab2) {
                    IBlockState up2 = playerWorld.getBlockState(dungeonRoom.getMin().add(newX, newY - 1, newZ));
                    if (up2.getBlock() == Blocks.stone_slab
                            || up2.getBlock() == Blocks.wooden_slab
                            || up2.getBlock() == Blocks.stone_slab2) {
                        PathPoint pt = openPoint(newX, newY, newZ);
                        if (pt.visited) continue;
                        pathOptions[i++] = pt;
                        continue;
                    }
                }
            }

            if (dir.getY() == 0
                    && curr.getBlock() == Blocks.iron_bars
                    && up.getBlock() == Blocks.air
                    && playerWorld.getBlockState(new BlockPos(currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord)).getBlock() != Blocks.iron_bars) {

                boolean theFlag = false;
                if (dir.getZ() == 0) {
                    if (playerWorld.getBlockState(
                            add1.add(0, 0, 1)).getBlock() == Blocks.air ||
                            playerWorld.getBlockState(add1.add(0, 0, -1)).getBlock() == Blocks.air) {
                        theFlag = true;
                    }
                } else if (dir.getX() == 0) {
                    if (playerWorld.getBlockState(add1.add(-1, 0, 0)).getBlock() == Blocks.air ||
                            playerWorld.getBlockState(add1.add(1, 0, 0)).getBlock() == Blocks.air) {
                        theFlag = true;
                    }
                }
                if (theFlag) {
                    PathPoint pt = openPoint(newX, newY, newZ);
                    if (pt.visited) continue;
                    pathOptions[i++] = pt;
                }
            }
        }
        return i;
    }

    public static final Set<Block> allowed = Sets.newHashSet(Blocks.air, Blocks.water, Blocks.lava, Blocks.flowing_water, Blocks.flowing_lava, Blocks.vine, Blocks.ladder
            , Blocks.standing_sign, Blocks.wall_sign, Blocks.trapdoor, Blocks.iron_trapdoor, Blocks.wooden_button, Blocks.stone_button, Blocks.fire,
            Blocks.torch, Blocks.rail, Blocks.golden_rail, Blocks.activator_rail, Blocks.detector_rail, Blocks.carpet, Blocks.redstone_torch);
    public static final IBlockState preBuilt = Blocks.stone.getStateFromMeta(2);

    public static boolean isValidBlock(IBlockState state) {
        return state.equals(preBuilt) || allowed.contains(state.getBlock());
    }
}
