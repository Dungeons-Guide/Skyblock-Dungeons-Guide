package kr.syeyoung.modapi.entity;

import kr.syeyoung.modapi.item.UItemStack;

public interface UEntityPlayerFake extends UEntityPlayer {
    void setCurrentArmor(int i, UItemStack itemStack);

    void setCurrentItem(int i);

    void setMainInventory(int i, UItemStack itemStack);
}
