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

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.impl.boss.FeatureChestPrice;
import kr.syeyoung.dungeonsguide.features.listener.GuiClickListener;
import kr.syeyoung.dungeonsguide.features.listener.KeyInputListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.client.event.GuiScreenEvent;

public class FeaturePressAnyKeyToCloseChest extends SimpleFeature implements KeyInputListener, GuiClickListener {
    public FeaturePressAnyKeyToCloseChest() {
        super("Dungeon", "Press Any Mouse Button or Key to close Secret Chest", "dungeon.presskeytoclose");
        parameters.put("threshold", new FeatureParameter<Integer>("threshold", "Price Threshold", "The maximum price of item for chest to be closed. Default 1m", 1000000, "integer"));
    }

    @Override
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent keyboardInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!isEnabled()) return;
        if (!DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnDungeon()) return;

        if (screen instanceof GuiChest){
            ContainerChest ch = (ContainerChest) ((GuiChest)screen).inventorySlots;
            if (!("Large Chest".equals(ch.getLowerChestInventory().getName())
                    || "Chest".equals(ch.getLowerChestInventory().getName()))) return;
            IInventory actualChest = ch.getLowerChestInventory();

            int priceSum = 0;
            for (int i = 0; i < actualChest.getSizeInventory(); i++) {
                priceSum += FeatureChestPrice.getPrice(actualChest.getStackInSlot(i));
            }

            int threshold = this.<Integer>getParameter("threshold").getValue();
            if (priceSum < threshold) {
                Minecraft.getMinecraft().thePlayer.closeScreen();
            }
        }
    }

    @Override
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!isEnabled()) return;
        if (!DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnDungeon()) return;

        if (screen instanceof GuiChest){
            ContainerChest ch = (ContainerChest) ((GuiChest)screen).inventorySlots;
            if (!("Large Chest".equals(ch.getLowerChestInventory().getName())
                    || "Chest".equals(ch.getLowerChestInventory().getName()))) return;
            IInventory actualChest = ch.getLowerChestInventory();

            int priceSum = 0;
            for (int i = 0; i < actualChest.getSizeInventory(); i++) {
                priceSum += FeatureChestPrice.getPrice(actualChest.getStackInSlot(i));
            }

            int threshold = this.<Integer>getParameter("threshold").getValue();
            if (priceSum < threshold) {
                Minecraft.getMinecraft().thePlayer.closeScreen();
            }
        }
    }
}
