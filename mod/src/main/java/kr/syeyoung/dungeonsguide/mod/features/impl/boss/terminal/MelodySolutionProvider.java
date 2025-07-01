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

public class MelodySolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(UContainerChest chest) {
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList());

        int target = -1;
        for (int i = 0; i < 5; i++) {
            UContainerSlot toChk = chest.getChestSlotAt(1 + i);
            if (toChk.getItemStack() != null &&
                    toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                    toChk.getItemStack().getItemColor() == EnumDyeColor.MAGENTA) { // MAGENTA
                target = i;
                break;
            }
        }

        int row = -1;
        for (int i = 0; i < 4; i++) {
            UContainerSlot toChk = chest.getChestSlotAt(16 + 9*i);

            if (toChk.getItemStack() != null &&
                    toChk.getItemStack().getItem() == Item.STAINED_HARDENED_CLAY &&
                    toChk.getItemStack().getItemColor() == EnumDyeColor.LIME) { // LIME
                row = i;
                break;
            }
        }
        UContainerSlot toChk = chest.getChestSlotAt(10 + target + 9 * row);
        if (toChk.getItemStack() != null &&
                toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE &&
                toChk.getItemStack().getItemColor() == EnumDyeColor.LIME) { // LIME
            ts.getCurrSlots().add(16 + 9*row);
        }

        return ts;
    }

    @Override
    public boolean isApplicable(UContainerChest chest) {
        return chest.getName().equals("Click the button on time!");
    }
}
