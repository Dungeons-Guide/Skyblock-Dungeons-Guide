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

import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;

import java.util.ArrayList;
import java.util.List;

public class SelectAllColorSolutionProvider implements TerminalSolutionProvider {
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
