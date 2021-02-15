package kr.syeyoung.dungeonsguide.roomprocessor;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomProcessorCreeperSolver extends GeneralRoomProcessor {

    private List<BlockPos[]> poses = new ArrayList<BlockPos[]>();

    private boolean bugged = false;

    public RoomProcessorCreeperSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);

        findCreeperAndDoPoses();
    }

    private void findCreeperAndDoPoses() {
        World w = getDungeonRoom().getContext().getWorld();
        List<BlockPos> prismarines = new ArrayList<BlockPos>();
        final BlockPos low = getDungeonRoom().getMin().add(0,-2,0);
        final BlockPos high = getDungeonRoom().getMax().add(0,20,0);
        final AxisAlignedBB axis = AxisAlignedBB.fromBounds(
                low.getX() + 15, low.getY(), low.getZ() + 15,
                high.getX() - 15, low.getY() + 10.5, high.getZ() - 15
        );

        for (BlockPos pos : BlockPos.getAllInBox(low, high)) {
            Block b = w.getBlockState(pos).getBlock();
            if (b == Blocks.prismarine || b == Blocks.sea_lantern) {
                for (EnumFacing face:EnumFacing.VALUES) {
                    if (w.getBlockState(pos.offset(face)).getBlock() == Blocks.air) {
                        prismarines.add(pos);
                        break;
                    }
                }
            }
        }

        while (prismarines.size() > 1) {
            BlockPos first = prismarines.get(0);
            BlockPos highestMatch = null;
            int highestDist = 0;
            label: for (int i = 1; i  < prismarines.size(); i++) {
                BlockPos second = prismarines.get(i);

                if (second.distanceSq(first) < highestDist) continue;

                double zslope = (second.getZ() - first.getZ()) / ((double)second.getX() - first.getX());
                double zIntercept = (first.getZ() - first.getX() * zslope);

                double yslope = (second.getY() - first.getY()) / ((double)second.getX() - first.getX());
                double yIntercept = (first.getY() - first.getX() * yslope);

                for (double x = axis.minX; x < axis.maxX; x += 0.1) {
                    double y = yslope * x + yIntercept;
                    double z = zslope * x + zIntercept;

                    if (y > axis.minY && y < axis.maxY && z > axis.minZ && z < axis.maxZ) {
                        // found pairll
                        highestDist = (int) second.distanceSq(first);
                        highestMatch = second;
                        break;
                    }
                }

                double xslope = (second.getX() - first.getX()) / ((double)second.getZ() - first.getZ());
                double xIntercept = (first.getX() - first.getZ() * xslope);

                yslope = (second.getY() - first.getY()) / ((double)second.getZ() - first.getZ());
                yIntercept = (first.getY() - first.getZ() * yslope);

                for (double z = axis.minZ; z < axis.maxZ; z += 0.1) {
                    double y = yslope * z + yIntercept;
                    double x = xslope * z + xIntercept;

                    if (y > axis.minY && y < axis.maxY && x > axis.minX && x < axis.maxX) {
                        // found pair
                        highestDist = (int) second.distanceSq(first);
                        highestMatch = second;
                        break;
                    }
                }
            }


            if (highestMatch == null) {
                prismarines.remove(first);
            } else {
                prismarines.remove(first);
                prismarines.remove(highestMatch);
                poses.add(new BlockPos[] {first, highestMatch});
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (bugged) {
            findCreeperAndDoPoses();
        }
    }

    private static final Color[] colors = new Color[] {Color.red, Color.orange, Color.green, Color.cyan, Color.blue, Color.pink, Color.yellow, Color.darkGray, Color.lightGray};
    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_CREEPER.isEnabled()) return;
        World w = getDungeonRoom().getContext().getWorld();
        for (int i = 0; i < poses.size(); i++) {
            BlockPos[] poset = poses.get(i);
            Color color = colors[i % colors.length];
            boolean oneIsConnected = false;
            if (w.getChunkFromBlockCoords(poset[0]).getBlock(poset[0]) != Blocks.sea_lantern &&
                w.getChunkFromBlockCoords(poset[1]).getBlock(poset[1]) != Blocks.sea_lantern) {
                oneIsConnected = true;
            }
            RenderUtils.drawLine(new Vec3(poset[0].getX() +0.5, poset[0].getY() +0.5, poset[0].getZ()+0.5),
                    new Vec3(poset[1].getX() +0.5, poset[1].getY() +0.5, poset[1].getZ()+0.5), oneIsConnected ? new Color(0,0,0,50) : color, partialTicks, true);
        }
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorCreeperSolver> {
        @Override
        public RoomProcessorCreeperSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorCreeperSolver defaultRoomProcessor = new RoomProcessorCreeperSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
