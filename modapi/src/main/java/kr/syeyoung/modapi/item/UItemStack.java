package kr.syeyoung.modapi.item;


import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.List;

public interface UItemStack {
    Item getItem();

    int getMetadata();

    String getSkullTexture();

    CompoundBinaryTag serialize();

    List<String> getLore();
    String getDisplayName();

    String getSkyblockId();

    Object getItemStack();

    List<String> getNormalTooltip();
}
