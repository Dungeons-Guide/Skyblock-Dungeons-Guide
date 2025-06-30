package kr.syeyoung.modapi.gui;

public interface UContainerChest extends UContainer {
    public String getName();
    public int getChestContainerSize();
    public UContainerSlot getChestSlotAt(int slot);
}
