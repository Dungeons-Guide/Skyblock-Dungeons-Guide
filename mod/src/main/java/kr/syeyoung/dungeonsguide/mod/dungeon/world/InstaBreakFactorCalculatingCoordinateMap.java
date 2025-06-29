package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlock;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class InstaBreakFactorCalculatingCoordinateMap implements ICoordinateMap<InstaBreakFactorCalculatingCoordinateMap.BreakFactor> {
    private ICoordinateMap<UBlockState> map;
    private CoordinateMapBlockAccessible world;
    private AlgorithmSetting algorithmSetting;

    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    public InstaBreakFactorCalculatingCoordinateMap(ICoordinateMap<UBlockState> map, AlgorithmSetting algorithmSetting) {
        this.map = map;
        this.world = new CoordinateMapBlockAccessible(map);
        this.minX = map.getMinX(); this.minY = map.getMinY(); this.minZ = map.getMinZ();
        this.maxX = map.getMaxX(); this.maxY = map.getMaxY(); this.maxZ = map.getMaxZ();
        this.lenX = maxX - minX;
        this.lenY = maxY - minY;
        this.lenZ = maxZ - minZ;
        this.algorithmSetting = algorithmSetting;
    }

    private ThreadLocal<VectorI3D> blockPosThreadLocal = ThreadLocal.withInitial(() -> new VectorI3D(0,0,0));

    @Override
    public BreakFactor getBlock(int x, int y, int z) {
        UBlockState iBlockState = map.getBlock(x, y, z);
        VectorI3D pos = blockPosThreadLocal.get();
        pos.x = x; pos.y = y; pos.z =z;
        UBlock b = iBlockState.getBlock();
        if (iBlockState.isOf(BlockType.AIR)) return BreakFactor.INSTABREAK;
        if (b.getBlockHardness(world, pos) < 0) {
            return BreakFactor.NO;
        } else if (algorithmSetting.getPickaxeSpeed() > 0 &&
                (((algorithmSetting.getPickaxe().canHarvest(b)) &&
                        b.getBlockHardness(world, pos) <= algorithmSetting.getPickaxeSpeed() / 30.0) ||
                        (b.getBlockHardness(world, pos) <= algorithmSetting.getPickaxeSpeed() / 100.0))
        ) {
        } else if (algorithmSetting.getShovelSpeed() > 0
                && "shovel".equals(iBlockState.getHarvestTool())
                && b.getBlockHardness(world, pos) <= algorithmSetting.getShovelSpeed()) {
        } else if (algorithmSetting.getAxeSpeed() > 0 &&
                "axe".equals(iBlockState.getHarvestTool()) &&
                b.getBlockHardness(world, pos) <= algorithmSetting.getAxeSpeed()) {
        } else {
            if (!algorithmSetting.isAllowSlowStonkPath())
                return BreakFactor.NO;
            return BreakFactor.MAYBE_HARD;
        }
        return BreakFactor.INSTABREAK;
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return map.isInScope(x, y, z);
    }

    @AllArgsConstructor
    public enum BreakFactor {
        INSTABREAK(0), MAYBE_HARD(1), NO(99);

        @Getter
        private int factor;
    }
}
