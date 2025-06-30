package kr.syeyoung.modapi.gui;

public interface UContainer {
    public void closeContainer();
    public void clickSlot(int slot, EnumClickType clickType);
    public int getContainerSize();
    public UContainerSlot getSlotAt(int slot);

    public enum EnumClickType {
        CHOOSE
    }
}
