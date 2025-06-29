package kr.syeyoung.modapi.item;


import net.kyori.adventure.nbt.CompoundBinaryTag;

public interface UItemStack {
    Item getItem();

    int getMetadata();

    String getSkullTexture();

    CompoundBinaryTag serialize();
}
