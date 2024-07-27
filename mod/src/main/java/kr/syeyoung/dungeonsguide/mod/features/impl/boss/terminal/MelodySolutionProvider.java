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

public class MelodySolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(ContainerChest chest, List<Slot> clicked) {
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());

        int target = -1;
        for (int i = 0; i < 5; i++) {
            Slot toChk = chest.getSlot(1 + i);
            if (toChk.getHasStack() && toChk.getStack() != null &&
                    toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                    toChk.getStack().getItemDamage() == EnumDyeColor.MAGENTA.getMetadata()) {
                target = i;
                break;
            }
        }

        int row = -1;
        for (int i = 0; i < 4; i++) {
            Slot toChk = chest.getSlot(16 + 9*i);

            if (toChk.getHasStack() && toChk.getStack() != null &&
                    toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_hardened_clay) &&
                    toChk.getStack().getItemDamage() == EnumDyeColor.LIME.getMetadata()) {
                row = i;
                break;
            }
        }
        System.out.println(target +" / " + row);
        Slot toChk = chest.getSlot(10 + target + 9 * row);
        if (toChk.getHasStack() && toChk.getStack() != null &&
                toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                toChk.getStack().getItemDamage() == EnumDyeColor.LIME.getMetadata()) {
            ts.getCurrSlots().add(chest.getSlot(16 + 9 * row));
        }

        return ts;
    }

    @Override
    public boolean isApplicable(ContainerChest chest) {
        return chest.getLowerChestInventory().getName().equals("Click the button on time!");
    }
}
