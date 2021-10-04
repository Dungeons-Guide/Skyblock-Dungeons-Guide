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

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MTooltip;
import kr.syeyoung.dungeonsguide.gui.elements.MTooltipText;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.cursor.EnumCursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.List;

public class PanelPartyListElement extends MPanel {
    private PanelPartyFinder panelPartyFinder;
    private int slot;

    public PanelPartyListElement(PanelPartyFinder panelPartyFinder, int slot) {
        this.panelPartyFinder = panelPartyFinder;
        this.slot = slot;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(-1, 32);
    }

    private MTooltip mTooltip;

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        GuiCustomPartyFinder guiCustomPartyFinder = panelPartyFinder.getGuiCustomPartyFinder();
        if (guiCustomPartyFinder.getGuiChest() == null) return;
        Slot s = guiCustomPartyFinder.getGuiChest().inventorySlots.getSlot(slot);
        ItemStack itemStack = s.getStack();
        if (itemStack == null) return;


        int color = RenderUtils.blendAlpha(0x141414, 0.0f);

        String note = "";
        boolean cantjoin = false;
        if (itemStack.getItem() == Item.getItemFromBlock(Blocks.bedrock)) cantjoin = true;
        int minClass = -1, minDungeon = -1;
        int pplIn = 0;
        {
            NBTTagCompound stackTagCompound = itemStack.getTagCompound();
            if (stackTagCompound.hasKey("display", 10)) {
                NBTTagCompound nbttagcompound = stackTagCompound.getCompoundTag("display");

                if (nbttagcompound.getTagId("Lore") == 9) {
                    NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);

                    for (int i = 0; i < nbttaglist1.tagCount(); i++) {
                        String str = nbttaglist1.getStringTagAt(i);
                        if (str.startsWith("§7§7Note:")) {
                            note = str.substring(12);
                        } else if (str.startsWith("§7Class Level Required: §b")) {
                            minClass = Integer.parseInt(str.substring(26));
                        } else if (str.startsWith("§7Dungeon Level Required: §b")) {
                            minDungeon = Integer.parseInt(str.substring(28));
                        } else if (str.startsWith("§cRequires ")) cantjoin = true;
                        if (str.endsWith("§b)")) pplIn ++;
                    }
                }
            }
        }

        note = note.replaceAll("(?i)(S\\+)", "§6$1§r");
        note = note.replaceAll("(?i)(carry)", "§4$1§r");

        try {
            if (!panelPartyFinder.getHighlightNote().isEmpty()) {
                for (String s1 : panelPartyFinder.getHighlightNote().split(",")) {
                    note = note.replaceAll("(?i)(" + s1 + ")", "§e§l$1§r");
                }
            }
        } catch (Exception e) {}

        if (cantjoin) {}
        else if (clicked) {
            color = RenderUtils.blendAlpha(0x141414, 0.10f);
        } else if (lastAbsClip.contains(absMousex, absMousey) && (getTooltipsOpen() == 0 || (mTooltip != null && mTooltip.isOpen()))) {
            color = RenderUtils.blendAlpha(0x141414, 0.12f);
        }
        if (cantjoin) {}
        else if (note.contains("§e")) {
            color = RenderUtils.blendTwoColors(color, 0x44FFFF00);
        } else if (note.contains("§6")){
            color = RenderUtils.blendTwoColors(color, 0x44FFAA00);
        }
        Gui.drawRect(0,0,getBounds().width,getBounds().height,color);

        RenderItem renderItem=  Minecraft.getMinecraft().getRenderItem();
        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.scale(2,2,1);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        renderItem.renderItemAndEffectIntoGUI(itemStack, 0,0);
        GlStateManager.popMatrix();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.pushMatrix();
        GlStateManager.translate(37,(32 - 2*fr.FONT_HEIGHT)/2,0);
        GlStateManager.scale(2,2,1);
        String name = itemStack.getDisplayName();
        if (name.contains("'"))
            name = name.substring(0, name.indexOf("'"));
        fr.drawString(name, 0,0,-1);

        note = "§7("+pplIn+") §f"+note;
        fr.drawString(note, fr.getStringWidth("AAAAAAAAAAAAAAAA")+5, 0,-1);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        String sideNote = "";
        if (minClass != -1) sideNote += "§7CLv ≥§b"+minClass+" ";
        if (minDungeon != -1) sideNote += "§7DLv ≥§b"+minDungeon+" ";
        if (cantjoin) sideNote = "§cCan't join";
        sideNote = sideNote.trim();

        GlStateManager.translate(getBounds().width,(32 - 2*fr.FONT_HEIGHT)/2,0);
        GlStateManager.scale(2,2,0);
        GlStateManager.translate(-fr.getStringWidth(sideNote), 0,0);
        fr.drawString(sideNote, 0,0,-1);

        GlStateManager.popMatrix();
        if (lastAbsClip.contains(absMousex, absMousey) && (mTooltip == null || !mTooltip.isOpen()) && getTooltipsOpen() == 0) {
            if (mTooltip != null) mTooltip.close();
            List<String> list = itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
            for (int i = 0; i < list.size(); ++i) {
                if (i == 0) {
                    list.set(i, itemStack.getRarity().rarityColor + list.get(i));
                } else {
                    list.set(i, EnumChatFormatting.GRAY + list.get(i));
                }
            }
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            mTooltip = new MTooltipText(list);
            mTooltip.setScale(scaledResolution.getScaleFactor());
            mTooltip.open(this);
        } else if (!lastAbsClip.contains(absMousex, absMousey)){
            if (mTooltip != null)
            mTooltip.close();
            mTooltip = null;
        }
    }

    @Override
    public void setParent(MPanel parent) {
        super.setParent(parent);
        if (parent == null && mTooltip != null) {
            mTooltip.close();
            mTooltip = null;
        }
    }

    boolean clicked = false;
    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (lastAbsClip.contains(absMouseX, absMouseY)&& (getTooltipsOpen() == 0 || (mTooltip != null && mTooltip.isOpen()))) {
            clicked = true;

            GuiChest chest = panelPartyFinder.getGuiCustomPartyFinder().getGuiChest();
            Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, slot, 0, 0, Minecraft.getMinecraft().thePlayer);
        }
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {
        clicked = false;
    }

    @Override
    public void mouseMoved(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {
        if (lastAbsClip.contains(absMouseX, absMouseY) && (getTooltipsOpen() == 0 || (mTooltip != null && mTooltip.isOpen()))) {
            setCursor(EnumCursor.POINTING_HAND);
        }
    }
}
