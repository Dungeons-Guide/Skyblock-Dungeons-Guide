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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
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
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        solution = null;
        solutionProvider = null;
        clicked.clear();
        if (event.gui instanceof GuiChest) {
            ContainerChest cc = (ContainerChest) ((GuiChest) event.gui).inventorySlots;
            if (provider.isApplicable(cc)) {
                solution = provider.provideSolution(cc, clicked);
                this.solutionProvider = provider;
            }
        }
    }

    @DGEventHandler
    public void onTick(DGTickEvent tickEvent) {
        if (!isEnabled()) return;
        if (solutionProvider == null) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            solution = null;
            solutionProvider = null;
            clicked.clear();
            return;
        }
        ContainerChest cc = (ContainerChest) ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots;

        solution = solutionProvider.provideSolution(cc, clicked);
    }

    @DGEventHandler
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (solutionProvider == null) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            solution = null;
            solutionProvider = null;
            clicked.clear();
            return;
        }

        if (solution != null) {
            int i = 222;
            int j = i - 108;
            int ySize = j + (((ContainerChest)(((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots)).getLowerChestInventory().getSizeInventory() / 9) * 18;
            int left = (rendered.gui.width - 176) / 2;
            int top = (rendered.gui.height - ySize ) / 2;
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.colorMask(true, true, true, false);
            GlStateManager.translate(left, top, 0);
            if (solution.getCurrSlots() != null) {
                for (Slot currSlot : solution.getCurrSlots()) {
                    int x = currSlot.xDisplayPosition;
                    int y = currSlot.yDisplayPosition;
                    Gui.drawRect(x, y, x + 16, y + 16, 0x7700FFFF);
                }
            }
            if (solution.getNextSlots() != null) {
                for (Slot nextSlot : solution.getNextSlots()) {
                    int x = nextSlot.xDisplayPosition;
                    int y = nextSlot.yDisplayPosition;
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
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        if (!isEnabled()) return;
        if (Mouse.getEventButton() == -1) return;
        if (solutionProvider == null) return;
        if (solution == null) return;
        if (solution.getCurrSlots() == null) {
            return;
        }
        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;

        Slot s = chest.getSlotUnderMouse();
        if (solution.getCurrSlots().contains(s)) {
            clicked.add(s);
            // swap with middle click
//            mouseInputEvent.setCanceled(true);
//            Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, s.slotNumber, 0, 4, Minecraft.getMinecraft().thePlayer);
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
