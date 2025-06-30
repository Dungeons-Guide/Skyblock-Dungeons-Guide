package kr.syeyoung.modapi.entity;

import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.item.UInventoryPlayer;

public interface UPlayerSelf extends UEntityPlayer {
    String getClientBrand();
    UInventoryPlayer getInventory();
    UContainer getOpenContainer();

    void setOpenContainer(UContainer guiChest);
}
