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

    public RoomProcessorCreeperSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        World w = dungeonRoom.getContext().getWorld();
        Set<BlockPos> prismarines = new HashSet<BlockPos>();
        final BlockPos low = dungeonRoom.getMin();
        final BlockPos high = dungeonRoom.getMax();
        List<EntityCreeper> creeepr = w.getEntities(EntityCreeper.class, new Predicate<EntityCreeper>() {
            @Override
            public boolean apply(@Nullable EntityCreeper input) {
                if (input.isInvisible()) return false;
                      BlockPos pos = input.getPosition();
                        return low.getX() < pos.getX() && pos.getX() < high.getX()
                                && low.getZ() < pos.getZ() && pos.getZ() < high.getZ() && input.getName().toLowerCase().contains("blaze");
            }
        });
        if (creeepr.isEmpty()) return;
        EntityCreeper creeper = creeepr.get(0);
        Vec3 position = creeper.getPositionVector().addVector(0,1.5,0);

        for (BlockPos allInBox : BlockPos.getAllInBox(low, high)) {
            Block b = w.getChunkFromBlockCoords(allInBox).getBlock(allInBox);
            if (b == Blocks.prismarine || b == Blocks.sea_lantern) {
                if (prismarines.contains(allInBox)) continue;
                prismarines.add(allInBox);

                Vec3 vector = new Vec3(allInBox.getX() +0.5, allInBox.getY() +0.5, allInBox.getZ() +0.5);
                Vec3 pos = position.subtract(vector).normalize();

                BlockPos opposite = null;
                for (int i = 0; i < 28;i++) {
                    Vec3 result = vector.addVector(pos.xCoord * i, pos.yCoord * i, pos.zCoord * i);
                    BlockPos pos3 = new BlockPos(result);
                    if (w.getChunkFromBlockCoords(pos3).getBlock(pos3) != Blocks.air) {
                        opposite = pos3;
                    }
                }
                if (opposite == null) continue;

                BlockPos otherPrismarine = null;
                for (BlockPos inBox : BlockPos.getAllInBox(opposite.add(-2, -3, -2), opposite.add(2, 3, 2))) {
                    Block b3 = w.getChunkFromBlockCoords(inBox).getBlock(inBox);
                    if (b3 == Blocks.prismarine ||b == Blocks.sea_lantern) {
                        otherPrismarine = inBox;
                        break;
                    }
                }
                if (otherPrismarine == null) continue;

                prismarines.add(otherPrismarine);
                poses.add(new BlockPos[] {allInBox, otherPrismarine});
            }
        }

    }



    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        World w = getDungeonRoom().getContext().getWorld();
        for (BlockPos[] poset:poses) {
//            if (w.getChunkFromBlockCoords(poset[0]).getBlock(poset[0]) != Blocks.sea_lantern &&
//                w.getChunkFromBlockCoords(poset[1]).getBlock(poset[1]) != Blocks.sea_lantern) {
//                continue;
//            }
            RenderUtils.drawLine(poset[0], poset[1], new Color(0,255,255,255), partialTicks);
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
