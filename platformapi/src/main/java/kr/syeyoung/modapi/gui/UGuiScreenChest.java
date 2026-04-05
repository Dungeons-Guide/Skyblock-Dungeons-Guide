package kr.syeyoung.modapi.gui;

public interface UGuiScreenChest extends UGuiScreen {
    UContainerChest getContainer();

    UContainerSlot getSlotUnderMouse();
}
