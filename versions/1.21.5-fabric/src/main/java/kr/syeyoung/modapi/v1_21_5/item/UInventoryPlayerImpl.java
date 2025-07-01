package kr.syeyoung.modapi.v1_21_5.item;

import kr.syeyoung.modapi.item.UInventoryPlayer;
import kr.syeyoung.modapi.item.UItemStack;
import net.minecraft.entity.player.InventoryPlayer;

public class UInventoryPlayerImpl implements UInventoryPlayer {
    private InventoryPlayer delegate;

    public UInventoryPlayerImpl(InventoryPlayer delegate) {
        this.delegate = delegate;
    }

    public UItemStack[] getArmorInventory() {
        if (delegate.armorInventory == null) return null;
        UItemStack[] itemStacks = new UItemStack[delegate.armorInventory.length];
        for (int i = 0; i < itemStacks.length; i++) {
            itemStacks[i] = delegate.armorInventory[i] == null ? null : new UItemStackImpl(delegate.armorInventory[i]);
        }
        return itemStacks;
    }

    public UItemStack[] getMainInventory() {
        if (delegate.mainInventory == null) return null;
        UItemStack[] itemStacks = new UItemStack[delegate.mainInventory.length];
        for (int i = 0; i < itemStacks.length; i++) {
            itemStacks[i] = delegate.mainInventory[i] == null ? null : new UItemStackImpl(delegate.mainInventory[i]);
        }
        return itemStacks;
    }
}
