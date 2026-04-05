package kr.syeyoung.modapi.item;

import net.kyori.adventure.nbt.CompoundBinaryTag;

public interface IItemStackRegistry {
    UItemStack fromNBT(CompoundBinaryTag binaryTag);
}
