package kr.syeyoung.dungeonsguide.features.impl.boss.terminal;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class NavigateMazeSolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(ContainerChest chest, List<Slot> clicked) {
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        Slot solution = null;
        for (Slot inventorySlot : chest.inventorySlots) {
            if (inventorySlot.inventory != chest.getLowerChestInventory()) continue;
            if (inventorySlot.getHasStack() && inventorySlot.getStack() != null) {
                if (inventorySlot.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                        inventorySlot.getStack().getItemDamage() == EnumDyeColor.WHITE.getMetadata()) {
                    int x = inventorySlot.slotNumber % 9;
                    int y = inventorySlot.slotNumber / 9;

                    if (x > 0) {
                        Slot toChk =  chest.inventorySlots.get(y * 9 + x - 1);

                        if (toChk.getHasStack() && toChk.getStack() != null &&
                                toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                                toChk.getStack().getItemDamage() == EnumDyeColor.LIME.getMetadata()) {
                            solution = inventorySlot;
                            break;
                        }
                    }
                    if (x < 8) {
                        Slot toChk =  chest.inventorySlots.get(y * 9 + x + 1);

                        if (toChk.getHasStack() && toChk.getStack() != null &&
                                toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                                toChk.getStack().getItemDamage() == EnumDyeColor.LIME.getMetadata()) {
                            solution = inventorySlot;
                            break;
                        }
                    }
                    if (y > 0) {
                        Slot toChk =  chest.inventorySlots.get((y-1) * 9 + x);

                        if (toChk.getHasStack() && toChk.getStack() != null &&
                                toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                                toChk.getStack().getItemDamage() == EnumDyeColor.LIME.getMetadata()) {
                            solution = inventorySlot;
                            break;
                        }
                    }
                    if (y < chest.getLowerChestInventory().getSizeInventory() / 9 - 1) {
                        Slot toChk =  chest.inventorySlots.get((y+1) * 9 + x);

                        if (toChk.getHasStack() && toChk.getStack() != null &&
                                toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                                toChk.getStack().getItemDamage() == EnumDyeColor.LIME.getMetadata()) {
                            solution = inventorySlot;
                            break;
                        }
                    }
                }
            }
        }

        if (solution == null) return null;
        ts.getCurrSlots().add(solution);
        ts.setNextSlots(new ArrayList<Slot>());
        {
            int x = solution.slotNumber % 9;
            int y = solution.slotNumber / 9;

            if (x > 0) {
                Slot toChk =  chest.inventorySlots.get(y * 9 + x - 1);

                if (toChk.getHasStack() && toChk.getStack() != null &&
                        toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                        toChk.getStack().getItemDamage() == EnumDyeColor.WHITE.getMetadata()) {
                    ts.getNextSlots().add(toChk);
                    return ts;
                }
            }
            if (x < 8) {
                Slot toChk =  chest.inventorySlots.get(y * 9 + x + 1);

                if (toChk.getHasStack() && toChk.getStack() != null &&
                        toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                        toChk.getStack().getItemDamage() == EnumDyeColor.WHITE.getMetadata()) {
                    ts.getNextSlots().add(toChk);
                    return ts;
                }
            }
            if (y > 0) {
                Slot toChk =  chest.inventorySlots.get((y-1) * 9 + x);

                if (toChk.getHasStack() && toChk.getStack() != null &&
                        toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                        toChk.getStack().getItemDamage() == EnumDyeColor.WHITE.getMetadata()) {
                    ts.getNextSlots().add(toChk);
                    return ts;
                }
            }
            if (y < chest.getLowerChestInventory().getSizeInventory() / 9 - 1) {
                Slot toChk =  chest.inventorySlots.get((y+1) * 9 + x);

                if (toChk.getHasStack() && toChk.getStack() != null &&
                        toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                        toChk.getStack().getItemDamage() == EnumDyeColor.WHITE.getMetadata()) {
                    ts.getNextSlots().add(toChk);
                    return ts;
                }
            }
        }

        return ts;
    }

    @Override
    public boolean isApplicable(ContainerChest chest) {
        return chest.getLowerChestInventory().getName().equals("Navigate the maze!");
    }
}
