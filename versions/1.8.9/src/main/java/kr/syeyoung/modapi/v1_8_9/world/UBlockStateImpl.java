package kr.syeyoung.modapi.v1_8_9.world;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.EnumHalf;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.UBlock;
import kr.syeyoung.modapi.world.UBlockState;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UBlockStateImpl implements UBlockState {
    private IBlockState delegate;
    private UBlockImpl block;
    private BlockStateRegistryImpl registry;

    private String id;
    protected UBlockStateImpl(IBlockState blockState, UBlockImpl block, BlockStateRegistryImpl blockStateRegistry) {
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
        return Block.getIdFromBlock(delegate.getBlock())+":"+delegate.getBlock().getMetaFromState(delegate);
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
        if (!delegate.getPropertyNames().contains(BlockLever.FACING)) return null;
        net.minecraft.util.EnumFacing facing =  delegate.getValue(BlockLever.FACING).getFacing();
        return EnumFacing.VALUES[facing.getIndex()];
    }


    @Override
    public UBlockState withFacing(EnumFacing facing) {
        PropertyDirection dir = registry.getDirectionMap()[Block.getIdFromBlock(delegate.getBlock())];
        if (dir == null) return  this;
        IBlockState newState = delegate.withProperty(dir, net.minecraft.util.EnumFacing.VALUES[facing.getIndex()]);
        return registry.getByStateId(Block.BLOCK_STATE_IDS.get(newState));
    }

    @Override
    public EnumFacing getAnyBlockFacingIfItExists() {
        PropertyDirection dir = registry.getDirectionMap()[Block.getIdFromBlock(delegate.getBlock())];
        if (dir == null) return  null;
        return EnumFacing.VALUES[delegate.getValue(dir).getIndex()];
    }

    public boolean hasTileEntity() {
        return block.getDelegate().hasTileEntity(delegate);
    }

    @Override
    public int getWaterLevel() {
        return delegate.getPropertyNames().contains(BlockDynamicLiquid.LEVEL) ? delegate.getValue(BlockDynamicLiquid.LEVEL) : 0;
    }

    @Override
    public int getColor() {
        return delegate.getPropertyNames().contains(BlockColored.COLOR) ? delegate.getValue(BlockColored.COLOR).getMetadata() : 0;
    }

    @Override
    public IBlockState getIBlockState() {
        return delegate;
    }



    private static final ThreadLocal<FakeWorld> fakeWorldGen = ThreadLocal.withInitial(() -> new FakeWorld());
    private static final ThreadLocal<BlockPos.MutableBlockPos> posGen = ThreadLocal.withInitial(() -> new BlockPos.MutableBlockPos(0,0,0));
    @Override
    public AABB getSelectedBoundingBox(IBlockAccessible blockAccessible, VectorI3D pos) {
        FakeWorld fakeWorld = fakeWorldGen.get();
        fakeWorld.setAccessible(blockAccessible);
        BlockPos.MutableBlockPos pos2 = posGen.get();
        pos2.set(pos.x, pos.y, pos.z);
        delegate.getBlock().setBlockBoundsBasedOnState(fakeWorld, pos2);
        AxisAlignedBB bb = block.getDelegate().getSelectedBoundingBox(fakeWorld, pos2);
        return new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    @Override
    public AABB getCollisionBoundingBox(IBlockAccessible theWorld, VectorI3D pos) {
        FakeWorld fakeWorld = fakeWorldGen.get();
        fakeWorld.setAccessible(theWorld);
        BlockPos.MutableBlockPos pos2 = posGen.get();
        pos2.set(pos.x, pos.y, pos.z);
        delegate.getBlock().setBlockBoundsBasedOnState(fakeWorld, pos2);
        AxisAlignedBB bb = block.getDelegate().getCollisionBoundingBox(fakeWorld, pos2, delegate);
        return new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);

    }

    @Override
    public boolean canCollideCheck(boolean hitIfLiquid) {
        return delegate.getBlock().canCollideCheck(delegate, hitIfLiquid);
    }

    @Override
    public int getHarvestLevel() {
        return delegate.getBlock().getHarvestLevel(delegate);
    }

    @Override
    public String getHarvestTool() {
        return delegate.getBlock().getHarvestTool(delegate);
    }



    private static final ThreadLocal<List<AxisAlignedBB>> listTemp = ThreadLocal.withInitial(() -> new ArrayList<>());
    @Override
    public void addCollisionBoxesToList(IBlockAccessible worldIn, VectorI3D pos, AABB mask, List<AABB> list) {

        FakeWorld fakeWorld = fakeWorldGen.get();
        fakeWorld.setAccessible(worldIn);
        BlockPos.MutableBlockPos pos2 = posGen.get();
        pos2.set(pos.x, pos.y, pos.z);

        List<AxisAlignedBB> result = listTemp.get();
        result.clear();

        delegate.getBlock().addCollisionBoxesToList(
                fakeWorld, pos2, delegate,
                new AxisAlignedBB(mask.minX, mask.minY, mask.minZ, mask.maxX, mask.maxY, mask.maxZ),
                result,
               null
        );

        for (AxisAlignedBB axisAlignedBB : result) {
            list.add(new AABB(
                    axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ,
                    axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ
            ));
        }
    }

    @Override
    public EnumHalf getStairHalf() {
        return delegate.getPropertyNames().contains(BlockStairs.HALF) ?
                delegate.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP ? EnumHalf.TOP : EnumHalf.BOTTOM : null;
    }

    @Override
    public int getLegacyId() {
        return Block.getIdFromBlock(delegate.getBlock());
    }

    @Override
    public int getLegacyMeta() {
        return delegate.getBlock().getMetaFromState(delegate);
    }


    @Override
    public int getLegacyStateId() {
        return Block.BLOCK_STATE_IDS.get(delegate);
    }

    public IBlockState getDelegate() {
        return delegate;
    }
}
