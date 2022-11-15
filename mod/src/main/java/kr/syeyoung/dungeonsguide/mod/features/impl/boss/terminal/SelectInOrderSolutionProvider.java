/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal;

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
                    && inventorySlot.getStack().getItemDamage() == EnumDyeColor.RED.getMetadata()) {
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
                    && inventorySlot.getStack().getItemDamage() == EnumDyeColor.RED.getMetadata()) {
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
