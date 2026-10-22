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

public class NavigateMazeSolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(ContainerScreen screen, List<Slot> clicked) {
        ChestMenu chest = screen.getMenu();
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        Slot solution = null;
        for (Slot inventorySlot : chest.slots) {
            if (inventorySlot.container != chest.getContainer()) continue;
            if (inventorySlot.hasItem() && !inventorySlot.getItem().isEmpty()) {
                if (inventorySlot.getItem().is(Items.WHITE_STAINED_GLASS_PANE)) {
                    int x = inventorySlot.index % 9;
                    int y = inventorySlot.index / 9;

                    if (x > 0) {
                        Slot toChk =  chest.slots.get(y * 9 + x - 1);

                        if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                                toChk.getItem().is(Items.LIME_STAINED_GLASS_PANE)) {
                            solution = inventorySlot;
                            break;
                        }
                    }
                    if (x < 8) {
                        Slot toChk =  chest.slots.get(y * 9 + x + 1);

                        if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                                toChk.getItem().is(Items.LIME_STAINED_GLASS_PANE)) {
                            solution = inventorySlot;
                            break;
                        }
                    }
                    if (y > 0) {
                        Slot toChk =  chest.slots.get((y-1) * 9 + x);

                        if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                                toChk.getItem().is(Items.LIME_STAINED_GLASS_PANE)) {
                            solution = inventorySlot;
                            break;
                        }
                    }
                    if (y < chest.getContainer().getContainerSize() / 9 - 1) {
                        Slot toChk =  chest.slots.get((y+1) * 9 + x);

                        if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                                toChk.getItem().is(Items.LIME_STAINED_GLASS_PANE)) {
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
            int x = solution.index % 9;
            int y = solution.index / 9;

            if (x > 0) {
                Slot toChk =  chest.slots.get(y * 9 + x - 1);

                if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                        toChk.getItem().is(Items.WHITE_STAINED_GLASS_PANE)) {
                    ts.getNextSlots().add(toChk);
                    return ts;
                }
            }
            if (x < 8) {
                Slot toChk =  chest.slots.get(y * 9 + x + 1);

                if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                        toChk.getItem().is(Items.WHITE_STAINED_GLASS_PANE)) {
                    ts.getNextSlots().add(toChk);
                    return ts;
                }
            }
            if (y > 0) {
                Slot toChk =  chest.slots.get((y-1) * 9 + x);

                if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                        toChk.getItem().is(Items.WHITE_STAINED_GLASS_PANE)) {
                    ts.getNextSlots().add(toChk);
                    return ts;
                }
            }
            if (y < chest.getContainer().getContainerSize() / 9 - 1) {
                Slot toChk =  chest.slots.get((y+1) * 9 + x);

                if (toChk.hasItem() && !toChk.getItem().isEmpty() &&
                        toChk.getItem().is(Items.WHITE_STAINED_GLASS_PANE)) {
                    ts.getNextSlots().add(toChk);
                    return ts;
                }
            }
        }

        return ts;
    }

    @Override
    public boolean isApplicable(ContainerScreen screen) {
        ChestMenu chest = screen.getMenu();
        return screen.getTitle().getString().equals("Navigate the maze!");
    }
}
