package kr.syeyoung.modapi.world;

import java.util.Set;

public interface IBlockRegistry {
    UBlockState fromSerializedSeting(String id);

    UBlockState oneFromWellknown(BlockType blockType);

    UBlockState fromOldId(int stateId);

    Set<UBlockState> getStatesByType(BlockType blockType);
}
