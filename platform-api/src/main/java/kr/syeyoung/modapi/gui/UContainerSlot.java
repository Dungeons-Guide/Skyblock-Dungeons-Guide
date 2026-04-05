package kr.syeyoung.modapi.gui;

import kr.syeyoung.modapi.item.UItemStack;

public interface UContainerSlot {
    int getX();
    int getY();
    int getSlotIndex();
    UItemStack getItemStack();
}
