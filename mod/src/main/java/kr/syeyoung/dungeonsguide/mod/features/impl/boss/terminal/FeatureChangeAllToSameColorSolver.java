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



import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.input.Mouse;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeatureChangeAllToSameColorSolver extends SimpleFeature {
    public FeatureChangeAllToSameColorSolver() {
        super("Bossfight.Floor 7.Terminal","F7 Change All To Same Color Terminal Solver", "Optimal solver for change to same color terminal", "bossfight.samecolorterminal");

        addParameter("cancelwrongclick", new FeatureParameter<>("cancelwrongclick", "Block non optimal clicks", "", true, TCBoolean.INSTANCE, nval -> block = nval));
    }

    private boolean block = true;

    private boolean isCorrectGui = false;
    private int[] solution = new int[9];
    private int targetColor;

    @DGEventHandler
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        isCorrectGui = false;
        if (event.gui instanceof GuiChest) {
            ContainerChest cc = (ContainerChest) ((GuiChest) event.gui).inventorySlots;
            if (cc.getLowerChestInventory().getName().equals("Change all to same color!")) {
                isCorrectGui = true;
            }
        }
    }

    @DGEventHandler
    public void onTick(DGTickEvent tickEvent) {
        if (!isEnabled()) return;
        if (!isCorrectGui) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            isCorrectGui = false;
            return;
        }
        ContainerChest cc = (ContainerChest) ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots;
        int[] currSlot = new int[9];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                Slot toChk = cc.getSlot(y * 9 + x + 12);
                if (toChk.getHasStack() && toChk.getStack() != null &&
                        toChk.getStack().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) {
                    int meta = toChk.getStack().getItemDamage();
                    int idx = y * 3 + x;
                    if (meta == EnumDyeColor.RED.getMetadata()) {
                        currSlot[idx] = 0;
                    } else if (meta == EnumDyeColor.ORANGE.getMetadata()) {
                        currSlot[idx] = 1;
                    } else if (meta == EnumDyeColor.YELLOW.getMetadata()) {
                        currSlot[idx] = 2;
                    } else if (meta == EnumDyeColor.GREEN.getMetadata()) {
                        currSlot[idx] = 3;
                    } else if (meta == EnumDyeColor.BLUE.getMetadata()) {
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
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (!isCorrectGui) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            isCorrectGui = false;
            return;
        }

        if (solution != null) {
            int i = 222;
            int j = i - 108;
            ContainerChest container = (ContainerChest) (((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots);
            IInventory inventory  = container.getLowerChestInventory();
            int ySize = j + (inventory.getSizeInventory() / 9) * 18;
            int left = (rendered.gui.width - 176) / 2;
            int top = (rendered.gui.height - ySize ) / 2;
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.colorMask(true, true, true, false);
            GlStateManager.translate(left, top, 0);

            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    int slotId = y * 9 + x + 12;
                    int clicks = solution[y * 3 + x];
                    Slot currSlot = container.getSlot(slotId);
                    int rx = currSlot.xDisplayPosition;
                    int ry = currSlot.yDisplayPosition;
                    Minecraft.getMinecraft().fontRendererObj
                            .drawString(String.valueOf(clicks), rx, ry, 0xFF00FF00);
                }
            }

            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.popMatrix();
        }
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
    }

    @DGEventHandler
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        if (!isEnabled()) return;
        if (Mouse.getEventButton() == -1) return;
        if (!isCorrectGui) return;

        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;

//        if (Mouse.getEventButton())

        Slot s = chest.getSlotUnderMouse();
        int row = s.slotNumber / 9;
        int column = s.slotNumber % 9;

        if (1 <= row && row <= 3 && 3 <= column && column <= 5) {
            int solutionSlotId = (row - 1) * 3 + column - 3;
            int clicks = solution[solutionSlotId];

            if ((clicks > 0 && Mouse.getEventButton() == 0) || (clicks < 0 && Mouse.getEventButton() == 1)) {
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
