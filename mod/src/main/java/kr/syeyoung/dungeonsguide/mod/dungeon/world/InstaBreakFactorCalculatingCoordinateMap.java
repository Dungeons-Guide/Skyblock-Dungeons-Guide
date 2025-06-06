package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCache;

public class InstaBreakFactorCalculatingCoordinateMap implements ICoordinateMap<InstaBreakFactorCalculatingCoordinateMap.BreakFactor> {
    private ICoordinateMap<IBlockState> map;
    private CoordinateMapWorld world;
    private AlgorithmSetting algorithmSetting;

    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    public InstaBreakFactorCalculatingCoordinateMap(ICoordinateMap<IBlockState> map, AlgorithmSetting algorithmSetting) {
        this.map = map;
        this.world = new CoordinateMapWorld(map);
        this.minX = map.getMinX(); this.minY = map.getMinY(); this.minZ = map.getMinZ();
        this.maxX = map.getMaxX(); this.maxY = map.getMaxY(); this.maxZ = map.getMaxZ();
        this.lenX = maxX - minX;
        this.lenY = maxY - minY;
        this.lenZ = maxZ - minZ;
        this.algorithmSetting = algorithmSetting;
    }

    private ThreadLocal<BlockPos.MutableBlockPos> blockPosThreadLocal = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    @Override
    public BreakFactor getBlock(int x, int y, int z) {
        IBlockState iBlockState = map.getBlock(x, y, z);
        BlockPos.MutableBlockPos pos = blockPosThreadLocal.get();
        pos.set(x, y, z);
        Block b = iBlockState.getBlock();
        if (b == Blocks.air) return BreakFactor.INSTABREAK;
        if (b.getBlockHardness(world, pos) < 0) {
            return BreakFactor.NO;
        } else if (algorithmSetting.getPickaxeSpeed() > 0 &&
                (((algorithmSetting.getPickaxe().getTool().canHarvestBlock(b)) &&
                        b.getBlockHardness(world, pos) <= algorithmSetting.getPickaxeSpeed() / 30.0) ||
                        (b.getBlockHardness(world, pos) <= algorithmSetting.getPickaxeSpeed() / 100.0))
        ) {
        } else if (algorithmSetting.getShovelSpeed() > 0
                && b.isToolEffective("shovel", iBlockState)
                && b.getBlockHardness(world, pos) <= algorithmSetting.getShovelSpeed()) {
        } else if (algorithmSetting.getAxeSpeed() > 0 && b.isToolEffective("axe", iBlockState) && b.getBlockHardness(world, pos) <= algorithmSetting.getAxeSpeed()) {
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
