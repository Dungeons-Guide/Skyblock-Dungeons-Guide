/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
