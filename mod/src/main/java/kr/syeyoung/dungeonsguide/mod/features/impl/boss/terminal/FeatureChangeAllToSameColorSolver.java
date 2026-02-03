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
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.gui.UContainerChest;
import kr.syeyoung.modapi.gui.UContainerSlot;
import kr.syeyoung.modapi.gui.UGuiScreenChest;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.util.EnumDyeColor;

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
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        isCorrectGui = false;
        if (event.getGui() instanceof UGuiScreenChest) {
            UContainerChest cc = ((UGuiScreenChest) event.getGui()).getContainer();
            if (cc.getName().equals("Change all to same color!")) {
                isCorrectGui = true;
            }
        }
    }

    @DGEventHandler
    public void onTick(ClientTickEvent tickEvent) {
        if (!isEnabled()) return;
        if (!isCorrectGui) return;
        if (!(ModAPI.getAPI().getCurrentGuiScreen() instanceof UGuiScreenChest)) {
            isCorrectGui = false;
            return;
        }

        UContainer cc2 = ModAPI.getAPI().getPlayer().getOpenContainer();
        if (!(cc2 instanceof UContainerChest)) return;
        UContainerChest cc = (UContainerChest) cc2;
        int[] currSlot = new int[9];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                UContainerSlot toChk = cc.getChestSlotAt(y * 9 + x + 12);
                if (toChk.getItemStack() != null&&
                        toChk.getItemStack().getItem() == Item.STAINED_GLASS_PANE) {
                    EnumDyeColor meta = toChk.getItemStack().getItemColor();
                    int idx = y * 3 + x;
                    if (meta == EnumDyeColor.RED) {
                        currSlot[idx] = 0;
                    } else if (meta == EnumDyeColor.ORANGE) {
                        currSlot[idx] = 1;
                    } else if (meta == EnumDyeColor.YELLOW) {
                        currSlot[idx] = 2;
                    } else if (meta == EnumDyeColor.GREEN) {
                        currSlot[idx] = 3;
                    } else if (meta == EnumDyeColor.BLUE) {
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
        if (!(ModAPI.getAPI().getCurrentGuiScreen() instanceof UGuiScreenChest)) {
            isCorrectGui = false;
            return;
        }

        if (solution != null) {

            RenderingContext context = new RenderingContext(rendered.getRenderContext());
            int i = 222;
            int j = i - 108;

            UContainer cc2 = ModAPI.getAPI().getPlayer().getOpenContainer();
            if (!(cc2 instanceof UContainerChest)) return;
            UContainerChest cc = (UContainerChest) cc2;

            int ySize = j + (cc.getChestContainerSize() / 9) * 18;
            int left = (rendered.getGui().getWidth() - 176) / 2;
            int top = (rendered.getGui().getHeight() - ySize ) / 2;
            context.ctx().pushMatrix();
//            GlStateManager.disableDepth();
//            GlStateManager.disableLighting();
//            GlStateManager.colorMask(true, true, true, false);
            context.ctx().translate(left, top, 0);

            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    int slotId = y * 9 + x + 12;
                    int clicks = solution[y * 3 + x];
                    UContainerSlot currSlot = cc.getChestSlotAt(slotId);
                    int rx = currSlot.getX();
                    int ry = currSlot.getY();
                    context.drawString(String.valueOf(clicks), rx, ry, 0xFF00FF00);
                }
            }

//            GlStateManager.colorMask(true, true, true, true);
            context.ctx().popMatrix();
        }
//        GlStateManager.enableBlend();
//        GlStateManager.enableLighting();
    }

    @DGEventHandler
    public void onMouseInput(ScreenMouseEvent.MouseClicked event) {
        if (!isEnabled()) return;
        if (!isCorrectGui) return;

        UGuiScreenChest chest = (UGuiScreenChest) ModAPI.getAPI().getCurrentGuiScreen();

        UContainerSlot s = chest.getSlotUnderMouse();
        if (s == null) return;
        int row = s.getSlotIndex() / 9;
        int column = s.getSlotIndex() % 9;

        if (1 <= row && row <= 3 && 3 <= column && column <= 5) {
            int solutionSlotId = (row - 1) * 3 + column - 3;
            int clicks = solution[solutionSlotId];

            if ((clicks > 0 && event.getEventButton() == 0) || (clicks < 0 && event.getEventButton() == 1)) {
                // correct.

            } else {
                if (block)
                    event.setCanceled(true);
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
