package v1_21_11.gui;

import kr.syeyoung.modapi.gui.UContainerChest;
import kr.syeyoung.modapi.gui.UContainerSlot;
import kr.syeyoung.modapi.v1_21_5.gui.UContainerImpl;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;

public class UContainerChestImpl extends UContainerImpl implements UContainerChest {
    protected GenericContainerScreenHandler delegate;
    protected Text title;
    public UContainerChestImpl(GenericContainerScreenHandler delegate, Text title) {
        super(delegate);
        this.delegate = delegate;
        this.title = title;
    }

    @Override
    public String getName() { // TODO: to comp.
        return delegate.getInventory() == null ? null : title.getLiteralString();
    }

    @Override
    public int getChestContainerSize() {
        return delegate.getRows() * 9;
    }

    @Override
    public UContainerSlot getChestSlotAt(int slot) {
        return delegate.getSlot(slot) == null ? null : new UContainerSlotImpl(delegate.getSlot(slot));
    }
}
