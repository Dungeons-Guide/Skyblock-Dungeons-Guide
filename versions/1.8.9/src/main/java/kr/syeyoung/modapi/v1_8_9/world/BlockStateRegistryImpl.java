package kr.syeyoung.modapi.v1_8_9.world;

import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.IBlockRegistry;
import kr.syeyoung.modapi.world.UBlock;
import kr.syeyoung.modapi.world.UBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ObjectIntIdentityMap;

public class BlockStateRegistryImpl implements IBlockRegistry {

    private ObjectIntIdentityMap<UBlockStateImpl> map = new ObjectIntIdentityMap<>();
    private UBlockImpl[] byId;


    public void init() {
        byId = new UBlockImpl[4096];
        for (Block block : Block.blockRegistry) {
            byId[Block.getIdFromBlock(block)] = new UBlockImpl(block);
        }

        map = new ObjectIntIdentityMap<>();
        for (IBlockState blockStateId : Block.BLOCK_STATE_IDS) {
            int stateId = Block.getStateId(blockStateId);
            map.put(new UBlockStateImpl(blockStateId, byId[Block.getIdFromBlock(blockStateId.getBlock())]), stateId);
        }
    }

    public UBlock getBlockById(int id) {
        return byId[id];
    }

    public UBlockState getByStateId(int stateId) {
        return map.getByValue(stateId);
    }

    @Override
    public UBlockState fromSerializedSeting(String id) {
        return null;
    }

    @Override
    public UBlockState oneFromWellknown(BlockType blockType) {
        if (blockType == BlockType.AIR) {
            return map.getByValue(0);
        }
        throw new IllegalArgumentException("Unsupported blocktype: "+blockType);
    }

    @Override
    public UBlockState fromOldId(int stateId) {
        return map.getByValue(stateId);
    }


}
