package kr.syeyoung.modapi.entity;

import kr.syeyoung.modapi.item.UItemStack;

public interface UEntityArmorStand extends UEntityLiving {
    UItemStack getEquipmentInSlot(int slotIn);

    boolean getAlwaysRenderNameTag();
}
