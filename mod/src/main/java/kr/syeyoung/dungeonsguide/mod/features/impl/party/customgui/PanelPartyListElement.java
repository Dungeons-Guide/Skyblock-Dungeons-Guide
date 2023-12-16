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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui;

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MTooltipText;
import kr.syeyoung.dungeonsguide.mod.utils.PartyFinderParty;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private ItemStack lastStack;
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        GuiCustomPartyFinder guiCustomPartyFinder = panelPartyFinder.getGuiCustomPartyFinder();
        if (guiCustomPartyFinder.getGuiChest() == null) return;
        Slot s = guiCustomPartyFinder.getGuiChest().inventorySlots.getSlot(slot);
        ItemStack itemStack = s.getStack();
        if (itemStack == null && lastStack == null) return;
        if (itemStack != null)
            lastStack = itemStack;
        else
            itemStack = lastStack;
        int color = RenderUtils.blendAlpha(0x141414, 0.0f);

        PartyFinderParty party = PartyFinderParty.fromItemStack(itemStack);
        String note = party.note;
        boolean notFound = false;
        boolean cantJoin = !party.canJoin;
        if (itemStack.getItem() == Item.getItemFromBlock(Blocks.bedrock)) {
            cantJoin = true;
            notFound = true;
        }
        int minClass = party.requiredClassLevel, minDungeon = party.requiredDungeonLevel;
        boolean nodupe = note.toLowerCase().contains("nodupe") || note.toLowerCase().contains("no dupe") || (note.toLowerCase().contains("nd") && (note.toLowerCase().indexOf("nd") == 0 || note.charAt(note.toLowerCase().indexOf("nd")-1) == ' '));

        note = note.replaceAll("(?i)(S\\+)", "§6$1§r");
        note = note.replaceAll("(?i)(carry)", "§4$1§r");

        try {
            if (!panelPartyFinder.getHighlightNote().isEmpty()) {
                for (String s1 : panelPartyFinder.getHighlightNote().split(",")) {
                    note = note.replaceAll("(?i)(" + s1 + ")", "§e§l$1§r");
                }
            }
        } catch (Exception e) {}

        if (cantJoin) {}
        else if (clicked) {
            color = RenderUtils.blendAlpha(0x141414, 0.10f);
        } else if (lastAbsClip.contains(absMousex, absMousey) && (getTooltipsOpen() == 0 || (mTooltip != null && mTooltip.isOpen()))) {
            color = RenderUtils.blendAlpha(0x141414, 0.12f);
        }
        if (cantJoin) {}
        else if (note.contains("§e")) {
            color = RenderUtils.blendTwoColors(color, 0x44FFFF00);
        } else if (note.contains("§6")){
            color = RenderUtils.blendTwoColors(color, 0x44FFAA00);
        }

        if (nodupe && party.classes.contains(FeatureRegistry.PARTYKICKER_CUSTOM.getLastClass())) {
            color = RenderUtils.blendTwoColors(color, 0x44FF0000);
            note = note.replace("nodupe", "§cnodupe§r").replace("no dupe", "§cno dupe§r").replace("nd", "§cnd§r");
        }
        Gui.drawRect(0,0,getBounds().width,getBounds().height,color);

        RenderItem renderItem=  Minecraft.getMinecraft().getRenderItem();
        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.scale(2,2,1);
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

        if (!notFound)
            note = "§7("+party.classes.size()+") §f"+note;
        fr.drawString(note, fr.getStringWidth("AAAAAAAAAAAAAAAA")+5, 0,-1);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        String sideNote = "";
        if (minClass > 0) sideNote += "§7CLv ≥§b"+minClass+" ";
        if (minDungeon > 0) sideNote += "§7DLv ≥§b"+minDungeon+" ";
        if (cantJoin && !notFound) sideNote = "§cCan't join";
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
