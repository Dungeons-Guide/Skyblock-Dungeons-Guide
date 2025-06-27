package kr.syeyoung.modapi.world;

public interface IBlockRegistry {
    UBlockState fromSerializedSeting(String id);

    UBlockState oneFromWellknown(BlockType blockType);
}
