package kr.syeyoung.dungeonsguide.features.impl.boss.terminal;

import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
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

public class FeatureTerminalSolvers extends SimpleFeature implements GuiOpenListener, TickListener, GuiPostRenderListener, GuiClickListener, TooltipListener {
    public FeatureTerminalSolvers() {
        super("Bossfight","F7 GUI Terminal Solver", "Solve f7 gui terminals. (color, startswith, order, navigate, correct panes)", "bossfight.terminals");
    }

    public static final List<TerminalSolutionProvider> solutionProviders = new ArrayList<TerminalSolutionProvider>();

    static  {
        solutionProviders.add(new WhatStartsWithSolutionProvider());
        solutionProviders.add(new SelectAllColorSolutionProivider());
        solutionProviders.add(new SelectInOrderSolutionProvider());
        solutionProviders.add(new NavigateMazeSolutionProvider());
        solutionProviders.add(new CorrectThePaneSolutionProvider());
    }

    private TerminalSolutionProvider solutionProvider;
    private TerminalSolution solution;
    private List<Slot> clicked = new ArrayList<Slot>();

    @Override
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        solution = null;
        solutionProvider = null;
        clicked.clear();
        if (event.gui instanceof GuiChest) {
            ContainerChest cc = (ContainerChest) ((GuiChest) event.gui).inventorySlots;
            for (TerminalSolutionProvider solutionProvider : solutionProviders) {
                if (solutionProvider.isApplicable(cc)) {
                    solution = solutionProvider.provideSolution(cc, clicked);
                    this.solutionProvider = solutionProvider;
                }
            }
        }
    }

    @Override
    public void onTick() {
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

    @Override
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (!isEnabled()) return;
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
    }

    @Override
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        if (!isEnabled()) return;
        if (Mouse.getEventButton() == -1) return;
        if (solutionProvider == null) return;
        if (solution == null) return;
        if (solution.getCurrSlots() == null) {
            mouseInputEvent.setCanceled(true);
            return;
        }
        mouseInputEvent.setCanceled(true);
        Slot s = ((GuiChest) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse();
        if (solution.getCurrSlots().contains(s)) {
            clicked.add(s);
            Minecraft.getMinecraft().playerController.windowClick(((GuiChest)Minecraft.getMinecraft().currentScreen).inventorySlots.windowId, s.getSlotIndex(),
                    2, 0, Minecraft.getMinecraft().thePlayer);
            return;
        }
    }

    @Override
    public void onTooltip(ItemTooltipEvent event) {
        if (!isEnabled()) return;
        if (solutionProvider == null) return;
        event.toolTip.clear();
    }
}
