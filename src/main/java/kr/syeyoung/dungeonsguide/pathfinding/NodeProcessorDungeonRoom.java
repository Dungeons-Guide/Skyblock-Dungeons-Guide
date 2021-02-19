package kr.syeyoung.dungeonsguide.pathfinding;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.pathfinder.NodeProcessor;

public class NodeProcessorDungeonRoom extends NodeProcessor {
    private DungeonRoom dungeonRoom;
    private BlockPos sub;
    public NodeProcessorDungeonRoom(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
        sub = dungeonRoom.getMax().subtract(dungeonRoom.getMin());
    }

    @Override
    public PathPoint getPathPointTo(Entity entityIn) {
        return openPoint((int)entityIn.posX - dungeonRoom.getMin().getX(), (int)entityIn.posY - dungeonRoom.getMin().getY(),
                (int)entityIn.posZ - dungeonRoom.getMin().getZ());
    }

    @Override
    public PathPoint getPathPointToCoords(Entity entityIn, double x, double y, double z) {
        return openPoint((int)x- dungeonRoom.getMin().getX(), (int)y - dungeonRoom.getMin().getY(),
                (int)z - dungeonRoom.getMin().getZ());
    }

    private static final EnumFacing[] values2 = new EnumFacing[6];
    static {
        values2[0] = EnumFacing.DOWN;
        values2[1] = EnumFacing.NORTH;
        values2[2] = EnumFacing.SOUTH;
        values2[3] = EnumFacing.EAST;
        values2[4] = EnumFacing.WEST;
        values2[5] = EnumFacing.UP;
    }

    @Override
    public int findPathOptions(PathPoint[] pathOptions, Entity entityIn, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {

        int i = 0;
        for (EnumFacing ef:values2) {
            Vec3i dir = ef.getDirectionVec();
            int newX = currentPoint.xCoord + dir.getX();
            int newY = currentPoint.yCoord + dir.getY();
            int newZ = currentPoint.zCoord + dir.getZ();
            if (newX < 0 || newZ < 0) continue;
            if (newX > sub.getX()|| newZ > sub.getZ()) continue;
            IBlockState curr = entityIn.getEntityWorld().getBlockState(dungeonRoom.getMin().add(newX, newY, newZ));
            IBlockState up = entityIn.getEntityWorld().getBlockState(dungeonRoom.getMin().add(newX, newY + 1, newZ));
            if (isValidBlock(curr)
                && isValidBlock(up )) {
                PathPoint pt = openPoint(newX, newY, newZ);
                if (pt.visited) continue;
                pathOptions[i++] = pt;
                continue;
            }

            if (curr.getBlock() == Blocks.air) {
                if (up.getBlock() == Blocks.stone_slab
                || up.getBlock() == Blocks.wooden_slab
                || up.getBlock() == Blocks.stone_slab2) {
                    IBlockState up2 = entityIn.getEntityWorld().getBlockState(dungeonRoom.getMin().add(newX, newY -1, newZ));
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
        }
        return i;
    }

    public static boolean isValidBlock(IBlockState state) {
        return state.getBlock() == Blocks.air || state.getBlock() == Blocks.water || state.getBlock() == Blocks.lava
                || state.getBlock() == Blocks.flowing_water || state.getBlock() == Blocks.flowing_lava
                || state.getBlock() == Blocks.vine || state.getBlock() == Blocks.ladder
                || state.getBlock() == Blocks.standing_sign || state.getBlock() == Blocks.wall_sign
                || state.getBlock() == Blocks.trapdoor || state.getBlock() == Blocks.iron_trapdoor
                || state.getBlock() == Blocks.wooden_button || state.getBlock() == Blocks.stone_button
                || state.getBlock() == Blocks.fire || state.getBlock() == Blocks.torch
                || state.getBlock() == Blocks.rail || state.getBlock() == Blocks.golden_rail
                || state.getBlock() == Blocks.activator_rail || state.getBlock() == Blocks.activator_rail
                || state.getBlock() == Blocks.carpet
                || (state == Blocks.stone.getStateFromMeta(2));
    }
}
