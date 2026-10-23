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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import kr.syeyoung.dungeonsguide.mod.events.impl.ItemTooltipEvent;
import org.lwjgl.input.Mouse;

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
    private final List<Slot> clicked = new ArrayList<Slot>();

    @DGEventHandler
    public void onGuiOpen(ScreenOpenEvent event) {
        if (!isEnabled()) return;
        solution = null;
        solutionProvider = null;
        clicked.clear();
        if (event.gui instanceof ContainerScreen) {
            ContainerScreen screen = (ContainerScreen) event.gui;
            if (provider.isApplicable(screen)) {
                solution = provider.provideSolution(screen, clicked);
                this.solutionProvider = provider;
            }
        }
    }

    @DGEventHandler
    public void onTick(DGTickEvent tickEvent) {
        if (!isEnabled()) return;
        if (solutionProvider == null) return;
        if (!(Minecraft.getInstance().screen instanceof ContainerScreen)) {
            solution = null;
            solutionProvider = null;
            clicked.clear();
            return;
        }
        ContainerScreen screen = (ContainerScreen) Minecraft.getInstance().screen;

        solution = solutionProvider.provideSolution(screen, clicked);
    }

    @DGEventHandler
    public void onGuiPostRender(ScreenRenderEvent.Post rendered) {
        if (solutionProvider == null) return;
        if (!(Minecraft.getInstance().screen instanceof ContainerScreen)) {
            solution = null;
            solutionProvider = null;
            clicked.clear();
            return;
        }

        if (solution != null) {
            int i = 222;
            int j = i - 108;
            int ySize = j + (((ChestMenu)(((ContainerScreen) Minecraft.getInstance().screen).getMenu())).getContainer().getContainerSize() / 9) * 18;
            int left = (rendered.gui.width - 176) / 2;
            int top = (rendered.gui.height - ySize ) / 2;
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.colorMask(true, true, true, false);
            GlStateManager.translate(left, top, 0);
            if (solution.getCurrSlots() != null) {
                for (Slot currSlot : solution.getCurrSlots()) {
                    int x = currSlot.x;
                    int y = currSlot.y;
                    Gui.drawRect(x, y, x + 16, y + 16, 0x7700FFFF);
                }
            }
            if (solution.getNextSlots() != null) {
                for (Slot nextSlot : solution.getNextSlots()) {
                    int x = nextSlot.x;
                    int y = nextSlot.y;
                    Gui.drawRect(x, y, x + 16, y + 16, 0x77FFFF00);
                }
            }
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.popMatrix();
        }
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
    }

    @DGEventHandler
    public void onMouseInput(ScreenMouseEvent.Pre mouseInputEvent) {
        if (!isEnabled()) return;
        if (Mouse.getEventButton() == -1) return;
        if (solutionProvider == null) return;
        if (solution == null) return;
        if (solution.getCurrSlots() == null) {
            return;
        }
        ContainerScreen chest = (ContainerScreen) Minecraft.getInstance().screen;

        Slot s = chest.hoveredSlot;
        if (solution.getCurrSlots().contains(s)) {
            clicked.add(s);
            // swap with middle click
//            mouseInputEvent.setCanceled(true);
//            Minecraft.getInstance().gameMode.windowClick(chest.inventorySlots.windowId, s.slotNumber, 0, 4, Minecraft.getInstance().player);
        } else {
            if (block)
                mouseInputEvent.setCanceled(true);
        }
    }

    @DGEventHandler
    public void onTooltip(ItemTooltipEvent event) {
        if (!isEnabled()) return;
        if (solutionProvider == null) return;
        event.toolTip.clear();
    }
}
