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

package kr.syeyoung.dungeonsguide.features.impl.party.customgui;

import kr.syeyoung.dungeonsguide.events.WindowUpdateEvent;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.client.event.GuiOpenEvent;

public class FeatureCustomPartyFinder extends SimpleFeature implements GuiOpenListener, GuiUpdateListener {
    public FeatureCustomPartyFinder() {
        super("Party Kicker","Custom Party Finder","Custom Party Finder", "party.customfinder", true);
    }

    GuiCustomPartyFinder guiCustomPartyFinder;
    @Override
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null) guiCustomPartyFinder = null;
        if (!isEnabled()) return;
        if (!(event.gui instanceof GuiChest)) return;
        GuiChest chest = (GuiChest) event.gui;
        if (!(chest.inventorySlots instanceof ContainerChest)) return;
        ContainerChest containerChest = (ContainerChest) chest.inventorySlots;
        IInventory lower = containerChest.getLowerChestInventory();
        if (lower == null || !lower.getName().equals("Party Finder")) return;

        if (guiCustomPartyFinder == null) {
            guiCustomPartyFinder = new GuiCustomPartyFinder();
        }
        guiCustomPartyFinder.setGuiChest(chest);

        event.gui = guiCustomPartyFinder;
    }

    @Override
    public void onGuiUpdate(WindowUpdateEvent windowUpdateEvent) {
        if (guiCustomPartyFinder != null) {
            guiCustomPartyFinder.onChestUpdate(windowUpdateEvent);
        }
    }
}
