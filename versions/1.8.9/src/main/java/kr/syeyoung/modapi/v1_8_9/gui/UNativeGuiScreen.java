package kr.syeyoung.modapi.v1_8_9.gui;

import kr.syeyoung.modapi.gui.*;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;

public class UNativeGuiScreen implements UGuiScreen {

    private GuiScreen handle;
    public UNativeGuiScreen(GuiScreen handle) {
        this.handle =handle;
    }

    public GuiScreen getHandle() {
        return handle;
    }

    @Override
    public int getWidth() {
        return handle.width;
    }

    @Override
    public int getHeight() {
        return handle.height;
    }

    public static class UGuiScreenChatImpl extends UNativeGuiScreen implements UGuiScreenChat {
        public UGuiScreenChatImpl(GuiChat handle) {
            super(handle);
        }
    }

    public static class UGuiScreenChestImpl extends UNativeGuiScreen implements UGuiScreenChest {
        private GuiChest handle;
        public UGuiScreenChestImpl(GuiChest handle) {
            super(handle);
            this.handle = handle;
        }

        @Override
        public UContainerChest getContainer() {
            return new UContainerChestImpl((ContainerChest) handle.inventorySlots);
        }

        @Override
        public UContainerSlot getSlotUnderMouse() {
            Slot s = handle.getSlotUnderMouse();
            if (s == null) return null;
            return new UContainerSlotImpl(handle.getSlotUnderMouse());
        }
    }

    public static UNativeGuiScreen getUScreen(GuiScreen screen) {
        if (screen == null) return null;
        if (screen instanceof GuiChat) return new UGuiScreenChatImpl((GuiChat) screen);
        else if (screen instanceof GuiChest) return new UGuiScreenChestImpl((GuiChest) screen);
        else return new UNativeGuiScreen(screen);
    }
}
