package kr.syeyoung.dungeonsguide.features.impl.boss.terminal;

import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;

import java.util.ArrayList;
import java.util.List;

public class SelectAllColorSolutionProivider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(ContainerChest chest, List<Slot> clicked) {
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        String name = chest.getLowerChestInventory().getName();
        EnumDyeColor edc = null;
        for (EnumDyeColor edc2 : EnumDyeColor.values()) {
            if (name.contains(edc2.getName().toUpperCase().replace("_"," "))) {
                edc = edc2;
                break;
            }
        }
        if (edc == null) return null;

        for (Slot inventorySlot : chest.inventorySlots) {
            if (inventorySlot.inventory != chest.getLowerChestInventory()) continue;
            if (inventorySlot.getHasStack() && inventorySlot.getStack() != null && !inventorySlot.getStack().isItemEnchanted()) {
                if (inventorySlot.getStack().getItem() != Items.dye && inventorySlot.getStack().getItemDamage() == edc.getMetadata())
                    ts.getCurrSlots().add(inventorySlot);
                else if (inventorySlot.getStack().getItem() == Items.dye && inventorySlot.getStack().getItemDamage() == edc.getDyeDamage())
                    ts.getCurrSlots().add(inventorySlot);
            }
        }
        return ts;
    }

    @Override
    public boolean isApplicable(ContainerChest chest) {
        return chest.getLowerChestInventory().getName().startsWith("Select all the ");
    }
}
