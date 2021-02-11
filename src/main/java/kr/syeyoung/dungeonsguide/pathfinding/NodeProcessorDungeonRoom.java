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

    @Override
    public int findPathOptions(PathPoint[] pathOptions, Entity entityIn, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {

        int i = 0;
        for (EnumFacing ef:EnumFacing.VALUES) {
            Vec3i dir = ef.getDirectionVec();
            int newX = currentPoint.xCoord + dir.getX();
            int newY = currentPoint.yCoord + dir.getY();
            int newZ = currentPoint.zCoord + dir.getZ();
            if (newX < 0 || newZ < 0) continue;
            if (newX > sub.getX()|| newZ > sub.getZ()) continue;
            IBlockState state = entityIn.getEntityWorld().getBlockState(dungeonRoom.getMin().add(newX, newY, newZ));
            if (state.getBlock() == Blocks.air || state.getBlock() == Blocks.water || state.getBlock() == Blocks.lava
            || state.getBlock() == Blocks.flowing_water || state.getBlock() == Blocks.flowing_lava) {
                PathPoint pt = openPoint(newX, newY, newZ);
                if (pt.visited) continue;
                pathOptions[i++] = pt;
            }
        }
        return i;
    }
}
