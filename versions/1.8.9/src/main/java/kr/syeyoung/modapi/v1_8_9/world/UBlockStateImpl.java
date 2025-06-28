package kr.syeyoung.modapi.v1_8_9.world;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlock;
import kr.syeyoung.modapi.world.UBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;

import java.util.Set;

public class UBlockStateImpl implements UBlockState {
    private IBlockState delegate;
    private UBlockImpl block;

    private String id;
    protected UBlockStateImpl(IBlockState blockState, UBlockImpl block) {
        this.delegate = blockState;
        id = block.getId();
        this.block = block;
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
        net.minecraft.util.EnumFacing facing =  delegate.getValue(BlockLever.FACING).getFacing();
        return EnumFacing.VALUES[facing.getIndex()];
    }

    public boolean hasTileEntity() {
        return block.getDelegate().hasTileEntity(delegate);
    }

    @Override
    public int getWaterLevel() {
        return delegate.getValue(BlockDynamicLiquid.LEVEL);
    }

    @Override
    public int getColor() {
        return delegate.getValue(BlockColored.COLOR).getMetadata();
    }

    @Override
    public Object getIBlockState() {
        return delegate;
    }
}
