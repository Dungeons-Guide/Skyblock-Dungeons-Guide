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
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.gui.elements.MList;
import kr.syeyoung.dungeonsguide.gui.elements.MScrollablePanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PanelPartyFinder extends MPanel {
    @Getter
    private GuiCustomPartyFinder guiCustomPartyFinder;

    private PanelPartyFinderSettings panelPartyFinderSettings;

    private MScrollablePanel scrollablePanel;
    private MList list;

    private MButton goBack;
    private MButton previous;
    private MButton next;
    private int page = 1;

    private Map<Integer, PanelPartyListElement> panelPartyListElementMap = new HashMap<>();

    public PanelPartyFinder(GuiCustomPartyFinder guiCustomPartyFinder) {
        this.guiCustomPartyFinder = guiCustomPartyFinder;

        scrollablePanel = new MScrollablePanel(1);
        panelPartyFinderSettings = new PanelPartyFinderSettings(this);

        list = new MList() {
            @Override
            public void resize(int parentWidth, int parentHeight) {
                setSize(new Dimension(parentWidth, 9999));
                realignChildren();
            }
        };
        list.setGap(1);

        scrollablePanel.add(list);

        add(scrollablePanel);
        add(panelPartyFinderSettings);

        previous = new MButton(); next = new MButton();
        previous.setText("Prev"); next.setText("Next");
        previous.setEnabled(false); next.setEnabled(false);
        next.setOnActionPerformed(() -> {
            GuiChest chest = getGuiCustomPartyFinder().getGuiChest();
            Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, 9*2+8, 0, 0, Minecraft.getMinecraft().thePlayer);
        });
        previous.setOnActionPerformed(() -> {
            GuiChest chest = getGuiCustomPartyFinder().getGuiChest();
            Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, 9*2, 0, 0, Minecraft.getMinecraft().thePlayer);
        });
        goBack = new MButton();
        goBack.setBackground(RenderUtils.blendAlpha(0xFF141414, 0.05f));
        goBack.setText("<");
        goBack.setOnActionPerformed(() -> {
            GuiChest chest = getGuiCustomPartyFinder().getGuiChest();
            Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, 9*5+3, 0, 0, Minecraft.getMinecraft().thePlayer);
        });
        add(previous); add(next); add(goBack);
    }

    public String getHighlightNote() {
        return panelPartyFinderSettings.getHighlightText();
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        scrollablePanel.setBounds(new Rectangle(0, fr.FONT_HEIGHT*2+41, 2*bounds.width/3, bounds.height - fr.FONT_HEIGHT*2-41));
        panelPartyFinderSettings.setBounds(new Rectangle(2*bounds.width/3+1, fr.FONT_HEIGHT*2+21, bounds.width/3 -1, (bounds.height-fr.FONT_HEIGHT*2-21)));

        previous.setBounds(new Rectangle(0, fr.FONT_HEIGHT*2+21, 50, 20));
        next.setBounds(new Rectangle(2*bounds.width/3-50, fr.FONT_HEIGHT*2+21, 50, 20));
        goBack.setBounds(new Rectangle(0,0, fr.FONT_HEIGHT*2+20, fr.FONT_HEIGHT*2+20));
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        // background
        Gui.drawRect(0,0,getBounds().width, getBounds().height, RenderUtils.blendAlpha(0xFF141414, 0.0f));
        // top bar
        Gui.drawRect(0,0,getBounds().width, fr.FONT_HEIGHT*2+21, RenderUtils.blendAlpha(0xFF141414, 0.05f));
        // lines
        Gui.drawRect(0,fr.FONT_HEIGHT*2+20,getBounds().width, fr.FONT_HEIGHT*2+21, -1);
        Gui.drawRect(panelPartyFinderSettings.getBounds().x-1,fr.FONT_HEIGHT*2+20,panelPartyFinderSettings.getBounds().x, getBounds().height, -1);
        // prev next bar
        Gui.drawRect(0,fr.FONT_HEIGHT*2+21,scrollablePanel.getBounds().width, fr.FONT_HEIGHT*2+41, RenderUtils.blendAlpha(0xFF141414, 0.08f));

        GlStateManager.pushMatrix();
            GlStateManager.translate(fr.FONT_HEIGHT*2+21, 0,0);
            GlStateManager.scale(2,2,1);
            fr.drawString("Party Finder", 5,5,-1);
        GlStateManager.popMatrix();
        fr.drawString("Page "+page, (scrollablePanel.getBounds().width-fr.getStringWidth("Page "+page))/2, (20-fr.FONT_HEIGHT)/2 + fr.FONT_HEIGHT*2+21, -1);
   }

    public synchronized void onChestUpdate(WindowUpdateEvent windowUpdateEvent) {
        if (windowUpdateEvent == null) {
            GuiChest guiChest = guiCustomPartyFinder.getGuiChest();
            if (guiChest == null) {
                panelPartyListElementMap.clear();
            } else {
                for (int x = 1; x<=7; x++) {
                    for (int y = 1; y <= 3; y++) {
                        int i = y * 9 + x;
                        Slot s = guiChest.inventorySlots.getSlot(i);
                        PanelPartyListElement prev = panelPartyListElementMap.remove(i);
                        if (s == null || !s.getHasStack()) { continue; }
                        if (!filter(s.getStack())) continue;

                        if (prev == null) prev = new PanelPartyListElement(this, i);
                        panelPartyListElementMap.put(i, prev);
                    }
                }

                {
                    Slot next = guiChest.inventorySlots.getSlot(9 * 2 + 8);
                    if (next.getStack() != null && next.getStack().getItem() == Items.arrow) {
                        this.next.setEnabled(true);
                        extractPage(next.getStack());
                    } else {
                        this.next.setEnabled(false);
                    }

                    Slot prev = guiChest.inventorySlots.getSlot(9 * 2 + 0);
                    if (prev.getStack() != null && prev.getStack().getItem() == Items.arrow) {
                        this.previous.setEnabled(true);
                        extractPage(prev.getStack());
                    } else {
                        this.previous.setEnabled(false);
                    }

                    Slot delist = guiChest.inventorySlots.getSlot(9 * 5 + 7);
                    panelPartyFinderSettings.setDelistable(delist.getStack() != null && delist.getStack().getItem() == Item.getItemFromBlock(Blocks.bookshelf));
                }
            }
        } else {
            if (windowUpdateEvent.getPacketSetSlot() != null) {
                int i = windowUpdateEvent.getPacketSetSlot().func_149173_d();

                ItemStack stack = windowUpdateEvent.getPacketSetSlot().func_149174_e();
                if (i == 9*2+8) {
                    if (stack != null && stack.getItem() == Items.arrow) {
                        this.next.setEnabled(true);
                        extractPage(stack);
                    } else {
                        this.next.setEnabled(false);
                    }
                } else if (i == 9*2) {
                    if (stack != null && stack.getItem() == Items.arrow) {
                        this.previous.setEnabled(true);
                        extractPage(stack);
                    } else {
                        this.previous.setEnabled(false);
                    }
                } else if (i == 9*5+7) {
                    panelPartyFinderSettings.setDelistable(stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.bookshelf));
                }

                if (i%9 == 0 || i%9 == 8 || i/9 == 0 || i/9 >= 4) {
                    return;
                }

                PanelPartyListElement prev = panelPartyListElementMap.remove(i);
                if (filter(stack)) {
                    if (prev == null) prev = new PanelPartyListElement(this, i);
                    panelPartyListElementMap.put(i, prev);
                }
            } else if (windowUpdateEvent.getWindowItems() != null) {
                for (int x = 1; x<=7; x++) {
                    for (int y = 1; y <= 3; y++) {
                        int i = y * 9 + x;
                        ItemStack item = windowUpdateEvent.getWindowItems().getItemStacks()[i];
                        PanelPartyListElement prev = panelPartyListElementMap.remove(i);
                        if (!filter(item)) continue;

                        if (prev == null) prev = new PanelPartyListElement(this, i);
                        panelPartyListElementMap.put(i, prev);
                    }
                }

                {
                    ItemStack next = windowUpdateEvent.getWindowItems().getItemStacks()[9 * 2 + 8];
                    if (next != null && next.getItem() == Items.arrow) {
                        this.next.setEnabled(true);
                        extractPage(next);
                    } else {
                        this.next.setEnabled(false);
                    }

                    ItemStack prev = windowUpdateEvent.getWindowItems().getItemStacks()[9 * 2];
                    if (prev != null && prev.getItem() == Items.arrow) {
                        this.previous.setEnabled(true);
                        extractPage(prev);
                    } else {
                        this.previous.setEnabled(false);
                    }

                    ItemStack delist = windowUpdateEvent.getWindowItems().getItemStacks()[9*5+7];
                    panelPartyFinderSettings.setDelistable(delist != null && delist.getItem() == Item.getItemFromBlock(Blocks.bookshelf));
                }
            }
        }

        addItems();
    }

    public boolean filter(ItemStack item) {
        return !(item == null || item.getItem() == null || item.getItem() == Item.getItemFromBlock(Blocks.air)) && panelPartyFinderSettings.filter(item);
    }

    public void extractPage(ItemStack itemStack) {
        NBTTagCompound stackTagCompound = itemStack.getTagCompound();
        if (stackTagCompound.hasKey("display", 10)) {
            NBTTagCompound nbttagcompound = stackTagCompound.getCompoundTag("display");

            if (nbttagcompound.getTagId("Lore") == 9) {
                NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);

                for (int i = 0; i < nbttaglist1.tagCount(); i++) {
                    String str = nbttaglist1.getStringTagAt(i);
                    if (str.startsWith("§ePage ")) {
                        int pg = Integer.parseInt(str.substring(7));
                        if (itemStack.getDisplayName().equals("§aPrevious Page")) page = pg+1;
                        else page = pg-1;
                    }
                }
            }
        }
    }

    public void addItems() {
        for (MPanel childComponent : list.getChildComponents()) {
            list.remove(childComponent);
        }
        for (Map.Entry<Integer, PanelPartyListElement> value : panelPartyListElementMap.entrySet()) {
            list.add(value.getValue());
        }
        scrollablePanel.evalulateContentArea();
    }
}
