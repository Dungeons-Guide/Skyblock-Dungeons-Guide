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

import java.util.ArrayList;
import java.util.List;

public class FeatureTerminalSolvers extends SimpleFeature {
    private TerminalSolutionProvider provider;
    public FeatureTerminalSolvers(TerminalSolutionProvider provider, String name, String description, String key) {
        super("Bossfight.Floor 7.Terminal",name, description, key);

        addParameter("cancelwrongclick", new FeatureParameter<>("cancelwrongclick", "Block invalid clicks", "", true, TCBoolean.INSTANCE, nval -> block = nval));
        this.provider = provider;
    }

    private boolean block = true;

//    public static final List<TerminalSolutionProvider> solutionProviders = new ArrayList<TerminalSolutionProvider>();

//    static  {
//        solutionProviders.add(new MelodySolutionProvider());
//    }

    private TerminalSolutionProvider solutionProvider;
    private TerminalSolution solution;
    private final List<UContainerSlot> clicked = new ArrayList<UContainerSlot>();

    @DGEventHandler
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        solution = null;
        solutionProvider = null;
        clicked.clear();
        if (event.getGui() instanceof UGuiScreenChest) {
            UContainerChest cc = ((UGuiScreenChest) event.getGui()).getContainer();
            if (provider.isApplicable(cc)) {
                solution = provider.provideSolution(cc);
                this.solutionProvider = provider;
            }
        }
    }

    @DGEventHandler
    public void onTick(ClientTickEvent tickEvent) {
        if (!isEnabled()) return;
        if (solutionProvider == null) return;
        if (!(ModAPI.getAPI().getCurrentGuiScreen() instanceof UGuiScreenChest)) {
            solution = null;
            solutionProvider = null;
            clicked.clear();
            return;
        }
        UContainer cc = ModAPI.getAPI().getPlayer().getOpenContainer();
        if (cc instanceof UContainerChest) {
            solution = solutionProvider.provideSolution((UContainerChest) cc);
        }
    }

    @DGEventHandler
    public void onGuiPostRender(ScreenRenderEvent.Post rendered) {
        if (solutionProvider == null) return;
        if (!(ModAPI.getAPI().getCurrentGuiScreen() instanceof UGuiScreenChest)) {
            solution = null;
            solutionProvider = null;
            clicked.clear();
            return;
        }
        UContainer cc = ModAPI.getAPI().getPlayer().getOpenContainer();
        if (!(cc instanceof UContainerChest)) {
            return;
        }
        UContainerChest containerChest = (UContainerChest) cc;

        if (solution != null) {
            RenderingContext context = new RenderingContext(rendered.getRenderContext());

            int i = 222;
            int j = i - 108;
            int ySize = j + (((UContainerChest) cc).getChestContainerSize() / 9) * 18;
            int left = (rendered.getGui().getWidth() - 176) / 2;
            int top = (rendered.getGui().getHeight() - ySize ) / 2;
            context.ctx().pushMatrix();
//            GlStateManager.disableDepth();
//            GlStateManager.disableLighting();
//            GlStateManager.colorMask(true, true, true, false);
            context.ctx().translate(left, top, 0);
            if (solution.getCurrSlots() != null) {
                for (Integer currSlot : solution.getCurrSlots()) {

                    int x = containerChest.getChestSlotAt(currSlot).getX();
                    int y = containerChest.getChestSlotAt(currSlot).getY();
                    context.drawRect(x, y, x + 16, y + 16, 0x7700FFFF);
                }
            }
            if (solution.getNextSlots() != null) {
                for (Integer nextSlot : solution.getNextSlots()) {
                    int x = containerChest.getChestSlotAt(nextSlot).getX();
                    int y = containerChest.getChestSlotAt(nextSlot).getY();
                    context.drawRect(x, y, x + 16, y + 16, 0x77FFFF00);
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
        if (solutionProvider == null) return;
        if (solution == null) return;
        if (solution.getCurrSlots() == null) {
            return;
        }
        UGuiScreenChest chest = (UGuiScreenChest) ModAPI.getAPI().getCurrentGuiScreen();

//        if (Mouse.getEventButton())

        UContainerSlot s = chest.getSlotUnderMouse();
        if (s == null) return;

        if (solution.getCurrSlots().contains(s.getSlotIndex())) {
            clicked.add(s);
            // swap with middle click
//            mouseInputEvent.setCanceled(true);
//            Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, s.slotNumber, 0, 4, Minecraft.getMinecraft().thePlayer);
        } else {
            if (block)
                event.setCanceled(true);
        }
    }

    @DGEventHandler
    public void onTooltip(ItemTooltipEvent event) {
        if (!isEnabled()) return;
        if (solutionProvider == null) return;
        event.toolTip.clear();
    }
}
