package kr.syeyoung.modapi.v1_21_11.gui;

import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.gui.UContainerSlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class UContainerImpl implements UContainer {
    protected ScreenHandler delegate;
    public UContainerImpl(ScreenHandler delegate) {
        this.delegate = delegate;
    }


    @Override
    public void closeContainer() {
        ScreenHandler c = MinecraftClient.getInstance().player.currentScreenHandler;
        if (c != delegate) throw new IllegalStateException("Container is not open");
        MinecraftClient.getInstance().player.closeHandledScreen();
    }

    @Override
    public void clickSlot(int slot, EnumClickType clickType) {
        ScreenHandler c = MinecraftClient.getInstance().player.currentScreenHandler;
        if (c != delegate) throw new IllegalStateException("Container is not open");
        int mouse = 0, mode = 0;

        MinecraftClient.getInstance().interactionManager.clickSlot(
                c.syncId,
                slot,
                mouse,
                SlotActionType.PICKUP,
                MinecraftClient.getInstance().player
        );
    }

    @Override
    public int getContainerSize() {
        return delegate.slots.size();
    }

    @Override
    public UContainerSlot getSlotAt(int slot) {
        Slot s = delegate.getSlot(slot);
        return s == null ? null : new UContainerSlotImpl(s);
    }

    @Override
    public int getWindowId() {
        return delegate.syncId;
    }

    public ScreenHandler getDelegate() {
        return delegate;
    }
}
