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

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SelectInOrderSolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(ContainerScreen screen, List<Slot> clicked) {
        ChestMenu chest = screen.getMenu();
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        int lowest = 1000;
        Slot slotLowest = null;
        for (Slot inventorySlot : chest.slots) {
            if (inventorySlot.container != chest.getContainer()) continue;
            if (inventorySlot.hasItem() && !inventorySlot.getItem().isEmpty() && inventorySlot.getItem().is(Items.RED_STAINED_GLASS_PANE)) {
                if (inventorySlot.getItem().getCount() < lowest) {
                    lowest = inventorySlot.getItem().getCount();
                    slotLowest = inventorySlot;
                }
            }
        }
        if (slotLowest != null)
            ts.getCurrSlots().add(slotLowest);

        Slot next = null;
        for (Slot inventorySlot : chest.slots) {
            if (inventorySlot.container != chest.getContainer()) continue;
            if (inventorySlot.hasItem() && !inventorySlot.getItem().isEmpty() && inventorySlot.getItem().is(Items.RED_STAINED_GLASS_PANE)) {
                if (inventorySlot.getItem().getCount() == lowest + 1) {
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
    public boolean isApplicable(ContainerScreen screen) {
        ChestMenu chest = screen.getMenu();
        return screen.getTitle().getString().equals("Click in order!");
    }
}
