package kr.syeyoung.dungeonsguide.roomprocessor;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import scala.actors.threadpool.Arrays;

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
        Set<BlockPos> prismarines = new HashSet<BlockPos>();
        final BlockPos low = getDungeonRoom().getMin().add(0,-3,0);
        final BlockPos high = getDungeonRoom().getMax().add(0,15,0);
        List<EntityCreeper> creeepr = w.getEntities(EntityCreeper.class, new Predicate<EntityCreeper>() {
            @Override
            public boolean apply(@Nullable EntityCreeper input) {
                if (input.isInvisible()) return false;
                BlockPos pos = input.getPosition();
                return low.getX() < pos.getX() && pos.getX() < high.getX()
                        && low.getZ() < pos.getZ() && pos.getZ() < high.getZ();
            }
        });
        if (creeepr.isEmpty()) {
            bugged = true;
            return;
        }
        EntityCreeper creeper = creeepr.get(0);
        Vec3 position = creeper.getPositionVector().addVector(0,1,0);

        for (BlockPos allInBox : BlockPos.getAllInBox(low, high)) {
            Block b = w.getChunkFromBlockCoords(allInBox).getBlock(allInBox);
            if (b == Blocks.prismarine || b == Blocks.sea_lantern) {
                if (prismarines.contains(allInBox)) continue;
                prismarines.add(allInBox);

                Vec3 vector = new Vec3(allInBox.getX() +0.5, allInBox.getY() +0.5, allInBox.getZ() +0.5);
                Vec3 pos = position.subtract(vector).normalize();
                BlockPos opposite = null;
                for (int i = 5; i < 28;i++) {
                    Vec3 result = vector.addVector(pos.xCoord * i, pos.yCoord * i, pos.zCoord * i);
                    BlockPos pos3 = new BlockPos(result);
                    if (w.getChunkFromBlockCoords(pos3).getBlock(pos3) != Blocks.air && pos3.distanceSq(creeper.getPosition()) > 5) {
                        opposite = pos3;
                    }
                }
                if (opposite == null) continue;

                BlockPos otherPrismarine = null;
                for (BlockPos inBox : BlockPos.getAllInBox(opposite.add(-2, -3, -2), opposite.add(2, 3, 2))) {
                    if (prismarines.contains(inBox)) continue;
                    if ( low.getX() < inBox.getX() && inBox.getX() < high.getX()
                            && low.getZ() < inBox.getZ() && inBox.getZ() < high.getZ()) {
                        Block b3 = w.getChunkFromBlockCoords(inBox).getBlock(inBox);
                        if (b3 == Blocks.prismarine || b3 == Blocks.sea_lantern) {
//                            for (EnumFacing ef : EnumFacing.values()) {
//                                BlockPos b4 = inBox.offset(ef);
//                                if (w.getChunkFromBlockCoords(b4).getBlock(b4) == Blocks.air) {
//                                    otherPrismarine = inBox;
//                                }
//                            }
//                            break;
                            otherPrismarine = inBox;
                            break;
                        }
                    }
                }
                if (otherPrismarine == null) continue;

                prismarines.add(otherPrismarine);
                poses.add(new BlockPos[] {allInBox, otherPrismarine});
            }
        }
        bugged = false;
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
