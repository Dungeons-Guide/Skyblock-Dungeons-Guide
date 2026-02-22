package v1_21_11.world;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.EnumHalf;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.util.EnumDyeColor;
import kr.syeyoung.modapi.v1_21_5.world.FakeWorld;
import kr.syeyoung.modapi.v1_21_5.world.UBlockImpl;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.UBlock;
import kr.syeyoung.modapi.world.UBlockState;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Set;

public class UBlockStateImpl implements UBlockState {
    private BlockState delegate;
    private UBlockImpl block;
    private BlockStateRegistryImpl registry;

    private String id;
    protected UBlockStateImpl(BlockState blockState, UBlockImpl block, BlockStateRegistryImpl blockStateRegistry) {
        this.delegate = blockState;
        id = block.getId();
        this.block = block;
        this.registry = blockStateRegistry;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public UBlock getBlock() {
        return block;
    }

    @Override
    public String serialize() {
        // universal.... id.... :/
//        return delegat;
        return getLegacyId()+":"+getLegacyMeta();
    }


    @Override
    public boolean isOf(BlockType... blocks) {
        for (BlockType blockType : blocks) {
            Set<UBlockState> blockstates = ModAPI.getAPI().getBlockRegistry().getStatesByType(blockType);
            if (blockstates.contains(this)) return true;
        }
        return false;
    }

    @Override
    public EnumFacing getLeverFacing() {
        if (!delegate.contains(LeverBlock.FACING)) return null;
        Direction facing = delegate.get(LeverBlock.FACING);
        return EnumFacing.VALUES[facing.getIndex()];
    }


    @Override
    public UBlockState withFacing(EnumFacing facing) {
        BlockState newBlockState;
        if (delegate.contains(Properties.FACING)) {
            newBlockState = delegate.with(Properties.FACING, Direction.byIndex(facing.getIndex()));
        } else if (delegate.contains(Properties.HOPPER_FACING)) {
            newBlockState = delegate.with(Properties.HOPPER_FACING, Direction.byIndex(facing.getIndex()));
        } else if (delegate.contains(Properties.HORIZONTAL_FACING)) {
            newBlockState = delegate.with(Properties.HORIZONTAL_FACING, Direction.byIndex(facing.getIndex()));
        } else {
            return this;
        }
        return registry.getByStateId(Block.STATE_IDS.getRawId(newBlockState));
    }

    @Override
    public EnumFacing getAnyBlockFacingIfItExists() {
        Direction direction;
        if (delegate.contains(Properties.FACING)) {
            direction = delegate.get(Properties.FACING);
        } else if (delegate.contains(Properties.HOPPER_FACING)) {
            direction = delegate.get(Properties.HOPPER_FACING);
        } else if (delegate.contains(Properties.HORIZONTAL_FACING)) {
            direction = delegate.get(Properties.HORIZONTAL_FACING);
        } else return null;
        return EnumFacing.VALUES[direction.getIndex()];
    }

    public boolean hasTileEntity() {
        return block.getDelegate() instanceof BlockWithEntity;
    }

    @Override
    public int getWaterLevel() {
        return delegate.getFluidState().getLevel();
    }

    @Override
    public EnumDyeColor getColor() {
        if (block.getDelegate() instanceof Stainable stainable)
            return EnumDyeColor.VALUES[stainable.getColor().getIndex()];
        if (block.getDelegate() instanceof TerracottaBlock terracottaBlock) {
            if (terracottaBlock == Blocks.WHITE_TERRACOTTA) return EnumDyeColor.WHITE;
            if (terracottaBlock == Blocks.ORANGE_TERRACOTTA) return EnumDyeColor.ORANGE;
            if (terracottaBlock == Blocks.MAGENTA_TERRACOTTA) return EnumDyeColor.MAGENTA;
            if (terracottaBlock == Blocks.LIGHT_BLUE_TERRACOTTA) return EnumDyeColor.LIGHT_BLUE;
            if (terracottaBlock == Blocks.YELLOW_TERRACOTTA) return EnumDyeColor.YELLOW;
            if (terracottaBlock == Blocks.LIME_TERRACOTTA) return EnumDyeColor.LIME;
            if (terracottaBlock == Blocks.PINK_TERRACOTTA) return EnumDyeColor.PINK;
            if (terracottaBlock == Blocks.GRAY_TERRACOTTA) return EnumDyeColor.GRAY;
            if (terracottaBlock == Blocks.CYAN_TERRACOTTA) return EnumDyeColor.CYAN;
            if (terracottaBlock == Blocks.PURPLE_TERRACOTTA) return EnumDyeColor.PURPLE;
            if (terracottaBlock == Blocks.BLUE_TERRACOTTA) return EnumDyeColor.BLUE;
            if (terracottaBlock == Blocks.BROWN_TERRACOTTA) return EnumDyeColor.BROWN;
            if (terracottaBlock == Blocks.GREEN_TERRACOTTA) return EnumDyeColor.GREEN;
            if (terracottaBlock == Blocks.RED_TERRACOTTA) return EnumDyeColor.RED;
            if (terracottaBlock == Blocks.BLACK_TERRACOTTA) return EnumDyeColor.BLACK;
        } else if (delegate.getBlock() instanceof ConcretePowderBlock terracottaBlock) {
            if (terracottaBlock == Blocks.WHITE_WOOL) return EnumDyeColor.WHITE;
            if (terracottaBlock == Blocks.ORANGE_WOOL) return EnumDyeColor.ORANGE;
            if (terracottaBlock == Blocks.MAGENTA_WOOL) return EnumDyeColor.MAGENTA;
            if (terracottaBlock == Blocks.LIGHT_BLUE_WOOL) return EnumDyeColor.LIGHT_BLUE;
            if (terracottaBlock == Blocks.YELLOW_WOOL) return EnumDyeColor.YELLOW;
            if (terracottaBlock == Blocks.LIME_WOOL) return EnumDyeColor.LIME;
            if (terracottaBlock == Blocks.PINK_WOOL) return EnumDyeColor.PINK;
            if (terracottaBlock == Blocks.GRAY_WOOL) return EnumDyeColor.GRAY;
            if (terracottaBlock == Blocks.LIGHT_GRAY_WOOL) return EnumDyeColor.SILVER;
            if (terracottaBlock == Blocks.CYAN_WOOL) return EnumDyeColor.CYAN;
            if (terracottaBlock == Blocks.PURPLE_WOOL) return EnumDyeColor.PURPLE;
            if (terracottaBlock == Blocks.BLUE_WOOL) return EnumDyeColor.BLUE;
            if (terracottaBlock == Blocks.BROWN_WOOL) return EnumDyeColor.BROWN;
            if (terracottaBlock == Blocks.GREEN_WOOL) return EnumDyeColor.GREEN;
            if (terracottaBlock == Blocks.RED_WOOL) return EnumDyeColor.RED;
            if (terracottaBlock == Blocks.BLACK_WOOL) return EnumDyeColor.BLACK;
            // dont care about conrete or powder concrete, since they didn't exist in 1.8. maybe change this impl later with smth faster.
        }
        return null;
    }

    @Override
    public BlockState getIBlockState() {
        return delegate;
    }



    private static final ThreadLocal<FakeWorld> fakeWorldGen = ThreadLocal.withInitial(() -> new FakeWorld());
    private static final ThreadLocal<BlockPos.Mutable> posGen = ThreadLocal.withInitial(() -> new BlockPos.Mutable(0,0,0));
    @Override
    public AABB getSelectedBoundingBox(IBlockAccessible blockAccessible, VectorI3D pos) {
//        FakeWorld fakeWorld = fakeWorldGen.get();
//        fakeWorld.setAccessible(blockAccessible);
//        BlockPos.Mutable pos2 = posGen.get();
//        pos2.set(pos.x, pos.y, pos.z);
//        delegate.getBlock().setBlockBoundsBasedOnState(fakeWorld, pos2);
//        AxisAlignedBB bb = block.getDelegate().getSelectedBoundingBox(fakeWorld, pos2);
//        return new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        return new AABB(pos.x, pos.y, pos.z, pos.x+1, pos.y+1, pos.z+1);
    }

    @Override
    public AABB getCollisionBoundingBox(IBlockAccessible theWorld, VectorI3D pos) {
//        FakeWorld fakeWorld = fakeWorldGen.get();
//        fakeWorld.setAccessible(theWorld);
//        BlockPos.Mutable pos2 = posGen.get();
//        pos2.set(pos.x, pos.y, pos.z);
//        delegate.getBlock().setBlockBoundsBasedOnState(fakeWorld, pos2);
//        AxisAlignedBB bb = block.getDelegate().getCollisionBoundingBox(fakeWorld, pos2, delegate);
//        return new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);

        return new AABB(pos.x, pos.y, pos.z, pos.x+1, pos.y+1, pos.z+1);
    }

    @Override
    public boolean canCollideCheck(boolean hitIfLiquid) {
//        delegate.getBlock
//        FakeWorld fakeWorld = fakeWorldGen.get();
//        fakeWorld.setAccessible(theWorld);
//        BlockPos.Mutable pos2 = posGen.get();
//        pos2.set(pos.x, pos.y, pos.z);
//        delegate.getBlock().setBlockBoundsBasedOnState(fakeWorld, pos2);
//        AxisAlignedBB bb = block.getDelegate().getCollisionBoundingBox(fakeWorld, pos2, delegate);
//        return new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
//        delegate.getCollisionShape().isEmpty();
        // everything is empty!!
//        return delegate.getBlock().canCollideCheck(delegate, hitIfLiquid);

        return false;
    }

    @Override
    public int getHarvestLevel() {
        // TODO: Change api, ondemand persisted stonk calc. instead of precalc.
        return 0;
    }

    @Override
    public String getHarvestTool() {
        // TODO: change api
        return "pickaxe";
    }



//    private static final ThreadLocal<List<AxisAlignedBB>> listTemp = ThreadLocal.withInitial(() -> new ArrayList<>());
    @Override
    public void addCollisionBoxesToList(IBlockAccessible worldIn, VectorI3D pos, AABB mask, List<AABB> list) {

        // TODO: Implement collision l8r.
        return;
//        FakeWorld fakeWorld = fakeWorldGen.get();
//        fakeWorld.setAccessible(worldIn);
//        BlockPos.Mutable pos2 = posGen.get();
//        pos2.set(pos.x, pos.y, pos.z);
//
//        List<Box> result = listTemp.get();
//        result.clear();
////        delegate.getCollisionShape(BlockView).
//
//
//        delegate.getBlock().addCollisionBoxesToList(
//                fakeWorld, pos2, delegate,
//                new AxisAlignedBB(mask.minX, mask.minY, mask.minZ, mask.maxX, mask.maxY, mask.maxZ),
//                result,
//               null
//        );
//
//        for (AxisAlignedBB axisAlignedBB : result) {
//            list.add(new AABB(
//                    axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ,
//                    axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ
//            ));
//        }
    }

    @Override
    public EnumHalf getStairHalf() {
        BlockHalf half = delegate.get(Properties.BLOCK_HALF, null);
        if (half == null) return null;
        if (half == BlockHalf.TOP) return EnumHalf.TOP;
        return EnumHalf.BOTTOM;
    }

    // Make legacy id mapper!
    @Override
    public int getLegacyId() {
        return 0; // TODO: TODO
    }

    @Override
    public int getLegacyMeta() {
        return 0; // TODO: TODO
    }


    @Override
    public int getLegacyStateId() {
        return 0; // TODO: TODO
    }

    public BlockState getDelegate() {
        return delegate;
    }
}
