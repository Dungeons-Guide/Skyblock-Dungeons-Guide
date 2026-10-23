package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import lombok.Getter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;


public class PearlCalculatingCoordinateMap implements ICoordinateMap<PearlCalculatingCoordinateMap.PearlLandType> {
    protected ICoordinateMap<BlockState> map;
    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    private BlockAndTintGetter world;
    private RoomBounds roomBounds;

    public PearlCalculatingCoordinateMap(ICoordinateMap<BlockState> map, RoomBounds roomBounds) {
        this.map = map;
        this.world = new CoordinateMapWorld(map).asVanilla();

        this.minX = roomBounds.getMinX() * 2 + 2;
        this.minY = 0;
        this.minZ = roomBounds.getMinZ() * 2 + 2;
        this.maxX = roomBounds.getMaxX() * 2 + 2;
        this.maxY = 256 * 2;
        this.maxZ = roomBounds.getMaxZ() * 2 + 2;

        this.lenX = maxX - minX;
        this.lenY = maxY - minY;
        this.lenZ = maxZ - minZ;
        this.roomBounds = roomBounds;
    }

    public PearlLandType getBlock(int x, int y, int z) {
        if (!roomBounds.canAccessRelative( (x - minX + 2) / 2, (z - minZ + 2) / 2)) return PearlLandType.BLOCKED;

        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;

        AABB pearlTest = new AABB(
                wX-0.3, wY-0.3, wZ-0.3, wX+ 0.3, wY+ 0.3, wZ + 0.3
        );

        int minX = Mth.floor(pearlTest.minX);
        int maxX = Mth.floor(pearlTest.maxX + 1.0D);
        int minY = Mth.floor(pearlTest.minY);
        int maxY = Mth.floor(pearlTest.maxY + 1.0D);
        int minZ = Mth.floor(pearlTest.minZ);
        int maxZ = Mth.floor(pearlTest.maxZ + 1.0D);

        // 26.1: volumes of the blocks' collision shapes inside the test cube and its top half, 1.8.9 summed clipped boxes
        VoxelShape pearlShape = Shapes.create(pearlTest);
        VoxelShape topShape = Shapes.create(new AABB(pearlTest.minX, pearlTest.minY + 0.3, pearlTest.minZ, pearlTest.maxX, pearlTest.maxY, pearlTest.maxZ));
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        boolean any = false;
        double wholeVolume = 0;
        double topVolume = 0;
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                for (int i2 = minY-1; i2 < maxY; ++i2) {
                    blockPos.set(k1, i2, l1);


                    BlockState state = map.getBlock(k1, i2, l1);
                    VoxelShape shape = state.getCollisionShape(world, blockPos).move(k1, i2, l1);
                    VoxelShape whole = Shapes.join(shape, pearlShape, BooleanOp.AND);
                    if (whole.isEmpty()) continue;
                    any = true;
                    for (AABB a : whole.toAabbs()) wholeVolume += a.getXsize() * a.getYsize() * a.getZsize();
                    for (AABB a : Shapes.join(shape, topShape, BooleanOp.AND).toAabbs()) topVolume += a.getXsize() * a.getYsize() * a.getZsize();
                }
            }
        }
        if (!any) return PearlLandType.OPEN;
        // total is 0.216
        if (wholeVolume > 0.215) return PearlLandType.BLOCKED;
        if (wholeVolume > 0.027 && 0 == topVolume) return PearlLandType.FLOOR;
        if (wholeVolume  == topVolume && wholeVolume > 0.027) return PearlLandType.CEILING;
        // floor wall and ceiling wall.
        if (wholeVolume - topVolume > 0.027 && topVolume > 0 && wholeVolume != topVolume * 2) return PearlLandType.FLOOR_WALL;
        if (wholeVolume > 0) return PearlLandType.WALL;
        return PearlLandType.OPEN;
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return !(x < minX || z < minZ || x >= maxX || z >= maxZ || y < minY || y+4 >= maxY);
    }

    public enum PearlLandType {
        UNCACHED, FLOOR, CEILING, FLOOR_WALL, WALL, BLOCKED, OPEN;

        public static final int BITS = (int) Math.ceil(Math.log(PearlLandType.values().length ) / Math.log(2));
        public static final PearlLandType[] VALUES = PearlLandType.values();
    }
}
