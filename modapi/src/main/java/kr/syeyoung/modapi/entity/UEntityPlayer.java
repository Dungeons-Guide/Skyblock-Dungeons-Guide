package kr.syeyoung.modapi.entity;

import kr.syeyoung.modapi.item.UItemStack;

public interface UEntityPlayer extends UEntityLiving {

    public String getSkinTexture();

    UItemStack getHeldItem();
}
