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

import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

public class SelectAllColorSolutionProvider implements TerminalSolutionProvider {
    @Override
    public TerminalSolution provideSolution(ContainerScreen screen, List<Slot> clicked) {
        ChestMenu chest = screen.getMenu();
        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        String name = screen.getTitle().getString();
        DyeColor edc = null;
        for (DyeColor edc2 : DyeColor.values()) {
            if (name.contains(edc2.getName().toUpperCase().replace("_"," "))) {
                edc = edc2;
                break;
            }
        }
        if (edc == null) return null;

        for (Slot inventorySlot : chest.slots) {
            if (inventorySlot.container != chest.getContainer()) continue;
            if (inventorySlot.hasItem() && !inventorySlot.getItem().isEmpty() && !inventorySlot.getItem().hasFoil()) {
                if (isColored(inventorySlot.getItem(), edc) || isDye(inventorySlot.getItem(), edc))
                    ts.getCurrSlots().add(inventorySlot);
            }
        }
        return ts;
    }

    // 1.8.9 matched the color by metadata on wool, stained glass (panes), stained clay and carpet;
    // 26.1 has an item per color (D125)
    private static boolean isColored(ItemStack stack, DyeColor color) {
        for (String kind : new String[] {"wool", "stained_glass", "stained_glass_pane", "terracotta", "carpet"}) {
            if (stack.is(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getName() + "_" + kind)))) return true;
        }
        return false;
    }

    // 1.8.9's dye item by dye damage: the dyes, and ink sac / cocoa beans / lapis / bone meal for their colors
    private static boolean isDye(ItemStack stack, DyeColor color) {
        if (stack.is(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getName() + "_dye")))) return true;
        switch (color) {
            case BLACK: return stack.is(Items.INK_SAC);
            case BROWN: return stack.is(Items.COCOA_BEANS);
            case BLUE: return stack.is(Items.LAPIS_LAZULI);
            case WHITE: return stack.is(Items.BONE_MEAL);
            default: return false;
        }
    }

    @Override
    public boolean isApplicable(ContainerScreen screen) {
        ChestMenu chest = screen.getMenu();
        return screen.getTitle().getString().startsWith("Select all the ");
    }
}
