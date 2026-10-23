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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenMouseEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenKeyboardEvent;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.TCInteger;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.Container;
import org.lwjgl.input.Mouse;

public class FeaturePressAnyKeyToCloseChest extends SimpleFeature {
    public FeaturePressAnyKeyToCloseChest() {
        super("Misc", "Press Any Mouse Button or Key to close Secret Chest", "dungeon.presskeytoclose");
//        addParameter("threshold", new FeatureParameter<Integer>("threshold", "Price Threshold", "The maximum price of item for chest to be closed. Default 1m", 1000000, TCInteger.INSTANCE));
    }

    @DGEventHandler
    public void onKeyInput(ScreenKeyboardEvent keyboardInputEvent) {
        GuiScreen screen = Minecraft.getInstance().screen;
        
        if (!SkyblockStatus.isOnDungeon()) return;

        if (screen instanceof ContainerScreen){
            String title = ((ContainerScreen)screen).getTitle().getString();
            if (!("Large Chest".equals(title)
                    || "Chest".equals(title))) return;
//            Container actualChest = ch.getLowerChestInventory();

//            int priceSum = 0;
//            for (int i = 0; i < actualChest.getSizeInventory(); i++) {
//                priceSum += FeatureChestPrice.getPrice(actualChest.getStackInSlot(i));
//            }

//            int threshold = this.<Integer>getParameter("threshold").getValue();
//            if (priceSum < threshold) {
                Minecraft.getInstance().player.closeContainer();
//            }
        }
    }

    @DGEventHandler
    public void onMouseInput(ScreenMouseEvent.Pre mouseInputEvent) {
        GuiScreen screen = Minecraft.getInstance().screen;
        
        if (!SkyblockStatus.isOnDungeon()) return;
        if (Mouse.getEventButton() == -1) return;

        if (screen instanceof ContainerScreen){
            String title = ((ContainerScreen)screen).getTitle().getString();
            if (!("Large Chest".equals(title)
                    || "Chest".equals(title))) return;
//            Container actualChest = ch.getLowerChestInventory();

//            int priceSum = 0;
//            for (int i = 0; i < actualChest.getSizeInventory(); i++) {
//                priceSum += FeatureChestPrice.getPrice(actualChest.getStackInSlot(i));
//            }

//            int threshold = this.<Integer>getParameter("threshold").getValue();
//            if (priceSum < threshold) {
                Minecraft.getInstance().player.closeContainer();
//            }
        }
    }
}
