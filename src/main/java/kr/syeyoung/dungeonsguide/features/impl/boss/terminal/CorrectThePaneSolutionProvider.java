package kr.syeyoung.dungeonsguide.features.impl.boss.terminal;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class CorrectThePaneSolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(ContainerChest chest, List<Slot> clicked) {
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        for (Slot inventorySlot : chest.inventorySlots) {
            if (inventorySlot.inventory != chest.getLowerChestInventory()) continue;
            if (inventorySlot.getHasStack() && inventorySlot.getStack() != null) {
                if (inventorySlot.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                        inventorySlot.getStack().getItemDamage() == EnumDyeColor.RED.getMetadata()) {
                    ts.getCurrSlots().add(inventorySlot);
                }
            }
        }


        return ts;
    }

    @Override
    public boolean isApplicable(ContainerChest chest) {
        return chest.getLowerChestInventory().getName().equals("Correct all the panes!");
    }
}
