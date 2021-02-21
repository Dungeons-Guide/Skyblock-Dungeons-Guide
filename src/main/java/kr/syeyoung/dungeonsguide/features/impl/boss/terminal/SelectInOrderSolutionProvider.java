package kr.syeyoung.dungeonsguide.features.impl.boss.terminal;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SelectInOrderSolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(ContainerChest chest, List<Slot> clicked) {
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        int lowest = 1000;
        Slot slotLowest = null;
        for (Slot inventorySlot : chest.inventorySlots) {
            if (inventorySlot.inventory != chest.getLowerChestInventory()) continue;
            if (inventorySlot.getHasStack() && inventorySlot.getStack() != null && inventorySlot.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)
                    && inventorySlot.getStack().getItemDamage() == EnumDyeColor.LIME.getMetadata()) {
                if (inventorySlot.getStack().stackSize < lowest) {
                    lowest = inventorySlot.getStack().stackSize;
                    slotLowest = inventorySlot;
                }
            }
        }
        if (slotLowest != null)
            ts.getCurrSlots().add(slotLowest);

        Slot next = null;
        for (Slot inventorySlot : chest.inventorySlots) {
            if (inventorySlot.inventory != chest.getLowerChestInventory()) continue;
            if (inventorySlot.getHasStack() && inventorySlot.getStack() != null && inventorySlot.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)
                    && inventorySlot.getStack().getItemDamage() == EnumDyeColor.LIME.getMetadata()) {
                if (inventorySlot.getStack().stackSize == lowest + 1) {
                    next = inventorySlot;
                }
            }
        }
        if (next != null) {
            ts.setNextSlots(new ArrayList<Slot>());
            ts.getNextSlots().add(next);
        }

        return ts;
    }

    @Override
    public boolean isApplicable(ContainerChest chest) {
        return chest.getLowerChestInventory().getName().equals("Click in order!");
    }
}
