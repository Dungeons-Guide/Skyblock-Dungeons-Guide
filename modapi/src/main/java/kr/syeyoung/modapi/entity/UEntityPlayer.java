package kr.syeyoung.modapi.entity;

import kr.syeyoung.modapi.item.UItemStack;

import java.util.UUID;

public interface UEntityPlayer extends UEntityLiving {

    public String getSkinTexture();

    UItemStack getHeldItem();

    UUID getUUID();

    void refreshDisplayName();
}
