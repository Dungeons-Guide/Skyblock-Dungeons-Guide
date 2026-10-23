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

import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class WhatStartsWithSolutionProvider implements TerminalSolutionProvider{
    @Override
    public TerminalSolution provideSolution(ContainerScreen screen, List<Slot> clicked) {
        ChestMenu chest = screen.getMenu();
        String that = screen.getTitle().getString().replace("What starts with: '", "").replace("'?", "").trim().toLowerCase();

        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        for (Slot inventorySlot : chest.slots) {
            if (inventorySlot.container != chest.getContainer()) continue;
            if (inventorySlot.hasItem() && !inventorySlot.getItem().isEmpty() && !inventorySlot.getItem().hasFoil() ) {
                String name = TextUtils.stripColor(inventorySlot.getItem().getDisplayName()).toLowerCase();
                if (name.startsWith(that))
                    ts.getCurrSlots().add(inventorySlot);
            }
        }
        return ts;
    }

    @Override
    public boolean isApplicable(ContainerScreen screen) {
        ChestMenu chest = screen.getMenu();
        return screen.getTitle().getString().startsWith("What starts with: '");
    }
}
