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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal;

import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class WhatStartsWithSolutionProvider implements TerminalSolutionProvider{
    @Override
    public TerminalSolution provideSolution(ContainerChest chest, List<Slot> clicked) {
        String that = chest.getLowerChestInventory().getName().replace("What starts with: '", "").replace("'?", "").trim().toLowerCase();

        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        for (Slot inventorySlot : chest.inventorySlots) {
            if (inventorySlot.inventory != chest.getLowerChestInventory()) continue;
            if (inventorySlot.getHasStack() && inventorySlot.getStack() != null && !inventorySlot.getStack().isItemEnchanted() ) {
                String name = TextUtils.stripColor(inventorySlot.getStack().getDisplayName()).toLowerCase();
                if (name.startsWith(that))
                    ts.getCurrSlots().add(inventorySlot);
            }
        }
        return ts;
    }

    @Override
    public boolean isApplicable(ContainerChest chest) {
        return chest.getLowerChestInventory().getName().startsWith("What starts with: '");
    }
}
