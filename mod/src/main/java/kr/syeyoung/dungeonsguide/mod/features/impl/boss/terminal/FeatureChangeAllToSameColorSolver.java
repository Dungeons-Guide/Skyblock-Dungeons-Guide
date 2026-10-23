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

import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenRenderEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenOpenEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenMouseEvent;


import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.client.input.MouseButtonEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ItemTooltipEvent;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeatureChangeAllToSameColorSolver extends SimpleFeature {
    public FeatureChangeAllToSameColorSolver() {
        super("Bossfight.Floor 7.Terminal","Change All To Same Color", "Optimal solver for change to same color terminal", "bossfight.samecolorterminal");

        addParameter("cancelwrongclick", new FeatureParameter<>("cancelwrongclick", "Block non optimal clicks", "", true, TCBoolean.INSTANCE, nval -> block = nval));
    }

    private boolean block = true;

    private boolean isCorrectGui = false;
    private int[] solution = new int[9];
    private int targetColor;

    @DGEventHandler
    public void onGuiOpen(ScreenOpenEvent event) {
        if (!isEnabled()) return;
        isCorrectGui = false;
        if (event.gui instanceof ContainerScreen) {
            if (((ContainerScreen) event.gui).getTitle().getString().equals("Change all to same color!")) {
                isCorrectGui = true;
            }
        }
    }

    @DGEventHandler
    public void onTick(DGTickEvent tickEvent) {
        if (!isEnabled()) return;
        if (!isCorrectGui) return;
        if (!(Minecraft.getInstance().screen instanceof ContainerScreen)) {
            isCorrectGui = false;
            return;
        }
        ChestMenu cc = (ChestMenu) ((ContainerScreen) Minecraft.getInstance().screen).getMenu();
        int[] currSlot = new int[9];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                Slot toChk = cc.getSlot(y * 9 + x + 12);
                if (toChk.hasItem() && !toChk.getItem().isEmpty()) {
                    // 1.8.9's stained glass pane metadata, the colored pane items on 26.1 (D125)
                    ItemStack pane = toChk.getItem();
                    int idx = y * 3 + x;
                    if (pane.is(Items.RED_STAINED_GLASS_PANE)) {
                        currSlot[idx] = 0;
                    } else if (pane.is(Items.ORANGE_STAINED_GLASS_PANE)) {
                        currSlot[idx] = 1;
                    } else if (pane.is(Items.YELLOW_STAINED_GLASS_PANE)) {
                        currSlot[idx] = 2;
                    } else if (pane.is(Items.GREEN_STAINED_GLASS_PANE)) {
                        currSlot[idx] = 3;
                    } else if (pane.is(Items.BLUE_STAINED_GLASS_PANE)) {
                        currSlot[idx] = 4;
                    }
                }
            }
        }


        int minCost = 100;
        int minCostSolId = 0;
        for (int solid = 0; solid < 5; solid++) {
            int culCost = 0;
            for (int slotId = 0; slotId < 9; slotId++) {
                // calc distance
                int straightDist = Math.abs(currSlot[slotId] - solid);
                int roundAbout = 5 - straightDist; // just think for a moment. it works.
                int minDist = Math.min(straightDist, roundAbout);

                culCost += minDist;
            }
            if (culCost < minCost) {
                minCost = culCost;
                minCostSolId = solid;
            }
        }
        targetColor = minCostSolId;


        // write solution
        for (int slotId = 0; slotId < 9; slotId++) {
            // calc distance
            int straightDist = Math.abs(currSlot[slotId] - targetColor);
            int roundAbout = 5 - straightDist; // just think for a moment. it works.
            int minDist = Math.min(straightDist, roundAbout);
            int mult = 1; // determine forward or backward?
            if (currSlot[slotId] < targetColor) { // if direct is going reverse, flip it.
                mult *= -1;
            }
            if (roundAbout > straightDist) { // if round about is faster, flip it.
                mult *= -1;
            }

            solution[slotId] = minDist * mult;
        }
    }

    @DGEventHandler
    public void onGuiPostRender(ScreenRenderEvent.Post rendered) {
        if (!isCorrectGui) return;
        if (!(Minecraft.getInstance().screen instanceof ContainerScreen)) {
            isCorrectGui = false;
            return;
        }

        if (solution != null) {
            int i = 222;
            int j = i - 108;
            ChestMenu container = (ChestMenu) (((ContainerScreen) Minecraft.getInstance().screen).getMenu());
            Container inventory  = container.getContainer();
            int ySize = j + (inventory.getContainerSize() / 9) * 18;
            int left = (rendered.gui.width - 176) / 2;
            int top = (rendered.gui.height - ySize ) / 2;
            // D126: drawn through the event's RenderingContext
            RenderingContext context = rendered.context;
            context.pose().pushMatrix();
            context.pose().translate(left, top);

            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    int slotId = y * 9 + x + 12;
                    int clicks = solution[y * 3 + x];
                    Slot currSlot = container.getSlot(slotId);
                    int rx = currSlot.x;
                    int ry = currSlot.y;
                    context.drawString(String.valueOf(clicks), rx, ry, 0xFF00FF00, false);
                }
            }

            context.pose().popMatrix();
        }
    }

    @DGEventHandler
    public void onMouseClick(ScreenMouseEvent.Click mouseInputEvent) {
        onMouseButton(mouseInputEvent, mouseInputEvent.buttonEvent);
    }

    @DGEventHandler
    public void onMouseRelease(ScreenMouseEvent.Release mouseInputEvent) {
        onMouseButton(mouseInputEvent, mouseInputEvent.buttonEvent);
    }

    @DGEventHandler
    public void onMouseDrag(ScreenMouseEvent.Drag mouseInputEvent) {
        onMouseButton(mouseInputEvent, mouseInputEvent.buttonEvent);
    }

    private void onMouseButton(ScreenMouseEvent mouseInputEvent, MouseButtonEvent buttonEvent) {
        if (!isEnabled()) return;
        if (!isCorrectGui) return;

        ContainerScreen chest = (ContainerScreen) Minecraft.getInstance().screen;

//        if ((mouseInputEvent.buttonEvent == null ? -1 : mouseInputEvent.buttonEvent.button()))

        Slot s = chest.hoveredSlot;
        if (s == null) return;
        int row = s.index / 9;
        int column = s.index % 9;

        if (1 <= row && row <= 3 && 3 <= column && column <= 5) {
            int solutionSlotId = (row - 1) * 3 + column - 3;
            int clicks = solution[solutionSlotId];

            if ((clicks > 0 && buttonEvent.button() == 0) || (clicks < 0 && buttonEvent.button() == 1)) {
                // correct.

            } else {
                if (block)
                    mouseInputEvent.setCanceled(true);
            }
        }


    }

    @DGEventHandler
    public void onTooltip(ItemTooltipEvent event) {
        if (!isEnabled()) return;
        if (!isCorrectGui) return;
        event.toolTip.clear();
    }
}
