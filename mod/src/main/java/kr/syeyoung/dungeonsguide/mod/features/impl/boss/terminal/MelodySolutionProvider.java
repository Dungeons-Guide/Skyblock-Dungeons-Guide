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

public class MelodySolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(ContainerScreen screen, List<Slot> clicked) {
        ChestMenu chest = screen.getMenu();
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());

        int target = -1;
        for (int i = 0; i < 5; i++) {
            Slot toChk = chest.getSlot(1 + i);
            if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                    toChk.getItem().is(Items.MAGENTA_STAINED_GLASS_PANE)) {
                target = i;
                break;
            }
        }

        int row = -1;
        for (int i = 0; i < 4; i++) {
            Slot toChk = chest.getSlot(16 + 9*i);

            if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                    toChk.getItem().is(Items.LIME_TERRACOTTA)) {
                row = i;
                break;
            }
        }
        Slot toChk = chest.getSlot(10 + target + 9 * row);
        if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                toChk.getItem().is(Items.LIME_STAINED_GLASS_PANE)) {
            ts.getCurrSlots().add(chest.getSlot(16 + 9 * row));
        }

        return ts;
    }

    @Override
    public boolean isApplicable(ContainerScreen screen) {
        ChestMenu chest = screen.getMenu();
        return screen.getTitle().getString().equals("Click the button on time!");
    }
}
