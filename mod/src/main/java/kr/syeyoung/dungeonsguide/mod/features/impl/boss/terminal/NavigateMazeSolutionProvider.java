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

import kr.syeyoung.modapi.gui.UContainerChest;
import kr.syeyoung.modapi.gui.UContainerSlot;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.util.EnumDyeColor;

import java.util.ArrayList;

public class NavigateMazeSolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(UContainerChest chest) {
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<>());
        int solution = -1;
        for (int slot = 0; slot< chest.getChestContainerSize(); slot++) {
            UContainerSlot slot1 = chest.getChestSlotAt(slot);
            if (slot1.getItemStack() != null) {
                if (slot1.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                        slot1.getItemStack().getItemColor() == EnumDyeColor.WHITE) { // WHITE
                    int x = slot % 9;
                    int y = slot / 9;

                    if (x > 0) {
                        UContainerSlot toChk =  chest.getSlotAt(y * 9 + x - 1);

                        if (toChk.getItemStack() != null &&
                                toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                                toChk.getItemStack().getItemColor() == EnumDyeColor.LIME) { // LIME .. below
                            solution = slot;
                            break;
                        }
                    }
                    if (x < 8) {
                        UContainerSlot toChk =  chest.getSlotAt(y * 9 + x + 1);

                        if (toChk.getItemStack() != null &&
                                toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                                toChk.getItemStack().getItemColor() == EnumDyeColor.LIME) { // LIME .. below
                            solution = slot;
                            break;
                        }
                    }
                    if (y > 0) {
                        UContainerSlot toChk =  chest.getSlotAt((y-1) * 9 + x);

                        if (toChk.getItemStack() != null &&
                                toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                                toChk.getItemStack().getItemColor() == EnumDyeColor.LIME) { // LIME .. below
                            solution = slot;
                            break;
                        }
                    }
                    if (y < chest.getChestContainerSize() / 9 - 1) {
                        UContainerSlot toChk =  chest.getSlotAt((y+1) * 9 + x);

                        if (toChk.getItemStack() != null &&
                                toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                                toChk.getItemStack().getItemColor() == EnumDyeColor.LIME) { // LIME .. below
                            solution = slot;
                            break;
                        }
                    }
                }
            }
        }

        if (solution == -1) return null;
        ts.getCurrSlots().add(solution);
        ts.setNextSlots(new ArrayList<>());
        {
            int x = solution % 9;
            int y = solution / 9;

            if (x > 0) {
                UContainerSlot toChk =  chest.getSlotAt(y * 9 + x - 1);

                if (toChk.getItemStack() != null &&
                        toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                        toChk.getItemStack().getItemColor() == EnumDyeColor.WHITE) { // LIME .. below
                    ts.getNextSlots().add(y*9+x-1);
                }
            }
            if (x < 8) {
                UContainerSlot toChk =  chest.getSlotAt(y * 9 + x + 1);

                if (toChk.getItemStack() != null &&
                        toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                        toChk.getItemStack().getItemColor() == EnumDyeColor.WHITE) { // LIME .. below
                    ts.getNextSlots().add(y*9+x+1);
                }
            }
            if (y > 0) {
                UContainerSlot toChk =  chest.getSlotAt((y-1) * 9 + x);

                if (toChk.getItemStack() != null &&
                        toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                        toChk.getItemStack().getItemColor() == EnumDyeColor.WHITE) { // LIME .. below
                    ts.getNextSlots().add((y-1)*9+x);
                }
            }
            if (y < chest.getChestContainerSize() / 9 - 1) {
                UContainerSlot toChk =  chest.getSlotAt((y+1) * 9 + x);

                if (toChk.getItemStack() != null &&
                        toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                        toChk.getItemStack().getItemColor() == EnumDyeColor.WHITE) { // LIME .. below
                    ts.getNextSlots().add((y+1)*9+x);
                }
            }
        }

        return ts;
    }

    @Override
    public boolean isApplicable(UContainerChest chest) {
        return chest.getName().equals("Navigate the maze!");
    }
}
