package kr.syeyoung.modapi.entity;

import kr.syeyoung.modapi.item.UItemStack;

public interface UEntityLiving extends UEntity {

    float getPrevRotationYawHead();

    float getRotationYawHead();

    float getHealth();

    UItemStack getCurrentArmor(int slotIn);

    UItemStack getHeldItem();

    Object getHandle();
}
