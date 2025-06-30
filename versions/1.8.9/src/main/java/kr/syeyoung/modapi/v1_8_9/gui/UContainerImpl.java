package kr.syeyoung.modapi.v1_8_9.gui;

import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.gui.UContainerSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class UContainerImpl implements UContainer {
    protected Container delegate;
    public UContainerImpl(Container delegate) {
        this.delegate = delegate;
    }


    @Override
    public void closeContainer() {
        Container c = Minecraft.getMinecraft().thePlayer.openContainer;
        if (c != delegate) throw new IllegalStateException("Container is not open");
        Minecraft.getMinecraft().thePlayer.closeScreen();
    }

    @Override
    public void clickSlot(int slot, EnumClickType clickType) {
        Container c = Minecraft.getMinecraft().thePlayer.openContainer;
        if (c != delegate) throw new IllegalStateException("Container is not open");
        int mouse = 0, mode = 0;
        Minecraft.getMinecraft().playerController.windowClick(
                c.windowId,
                slot,
                mouse,
                mode,
                Minecraft.getMinecraft().thePlayer
        );
    }

    @Override
    public int getContainerSize() {
        return delegate.inventorySlots.size();
    }

    @Override
    public UContainerSlot getSlotAt(int slot) {
        Slot s = delegate.inventorySlots.get(slot);
        return s == null ? null : new UContainerSlotImpl(s);
    }
}
