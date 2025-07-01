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

public class SelectAllColorSolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(UContainerChest chest) {
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList());
        String name = chest.getName();
        EnumDyeColor edc = null;
        for (EnumDyeColor edc2 : EnumDyeColor.VALUES) {
            if (name.contains(edc2.name().toUpperCase().replace("_"," "))) {
                edc = edc2;
                break;
            }
        }
        if (edc == null) return null;

        for (int slot = 0; slot< chest.getChestContainerSize(); slot++) {
            UContainerSlot slot1 = chest.getChestSlotAt(slot);
            if (slot1.getItemStack() != null && !slot1.getItemStack().isItemEnchanted()) {
                if (slot1.getItemStack().getItem() != Item.DYE && slot1.getItemStack().getItemColor() == edc)
                    ts.getCurrSlots().add(slot);
            }
        }
        return ts;
    }

    @Override
    public boolean isApplicable(UContainerChest chest) {
        return chest.getName().startsWith("Select all the ");
    }
}
