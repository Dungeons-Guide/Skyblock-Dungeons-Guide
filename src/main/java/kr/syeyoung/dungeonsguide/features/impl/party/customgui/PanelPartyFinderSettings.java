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

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.List;

public class PanelPartyFinderSettings extends MPanelScaledGUI {
    private PanelPartyFinder panelPartyFinder;

    private MButton refresh = new MButton(), createNew = new MButton(), settings = new MButton();
    private MPassiveLabelAndElement filterCantjoin, filterWhitelistNote, filterBlacklistNote, plaeHighlightNote; private MToggleButton filterCantjoinButton;
    private MTextField filterWhitelist, filterBlacklist, highlightNote;

    @Getter @Setter
    boolean delistable = false;

    public void setDelistable(boolean delistable) {
        createNew.setText(delistable ? "De-list" : "Create New");
        this.delistable = delistable;
    }

    public PanelPartyFinderSettings(PanelPartyFinder panelPartyFinder) {
        this.panelPartyFinder = panelPartyFinder;

        createNew.setText("Create New");
        createNew.setOnActionPerformed(this::createNew);
        createNew.setBackground(0xFF00838F);
        createNew.setHover(0xFF00ACC1);
        createNew.setClicked(0xFF0097A7);
        add(createNew);
        refresh.setText("Refresh");
        refresh.setOnActionPerformed(this::refresh);
        add(refresh);
        settings.setText("Search Settings");
        settings.setOnActionPerformed(this::settings);
        add(settings);

        {
            filterCantjoinButton = new MToggleButton();
            filterCantjoin =  new MPassiveLabelAndElement("Filter Unjoinable", filterCantjoinButton);
            filterCantjoin.setDivideRatio(0.7);
            filterCantjoinButton.setOnToggle(() -> panelPartyFinder.onChestUpdate(null));
            add(filterCantjoin);
        }
        {
            filterWhitelist = new MTextField() {
                @Override
                public void edit(String str) {
                    panelPartyFinder.onChestUpdate(null);
                    FeatureRegistry.PARTYKICKER_CUSTOM.setWhitelist(str);
                }
            };
            filterBlacklist = new MTextField() {
                @Override
                public void edit(String str) {
                    FeatureRegistry.PARTYKICKER_CUSTOM.setBlacklist(str);
                    panelPartyFinder.onChestUpdate(null);
                }
            };
            highlightNote = new MTextField() {
                @Override
                public void edit(String str) {
                    super.edit(str);
                    FeatureRegistry.PARTYKICKER_CUSTOM.setHighlight(str);
                }
            };

            filterWhitelist.setText(FeatureRegistry.PARTYKICKER_CUSTOM.getWhitelist());
            filterBlacklist.setText(FeatureRegistry.PARTYKICKER_CUSTOM.getBlacklist());
            highlightNote.setText(FeatureRegistry.PARTYKICKER_CUSTOM.getHighlight());

            filterWhitelistNote = new MPassiveLabelAndElement("Whitelist Note", filterWhitelist);
            filterBlacklistNote = new MPassiveLabelAndElement("Blacklist Note", filterBlacklist);
            plaeHighlightNote = new MPassiveLabelAndElement("Highlight Note", highlightNote);

            filterWhitelistNote.setDivideRatio(0.5);
            filterBlacklistNote.setDivideRatio(0.5);
            plaeHighlightNote.setDivideRatio(0.5);
            add(filterWhitelistNote);
            add(filterBlacklistNote);
            add(plaeHighlightNote);
        }
    }

    private void createNew() {
        GuiChest chest = panelPartyFinder.getGuiCustomPartyFinder().getGuiChest();
        if (delistable)
            Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, 9*5+7, 0, 0, Minecraft.getMinecraft().thePlayer);
        else
            Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, 9*5+0, 0, 0, Minecraft.getMinecraft().thePlayer);

    }

    public String getHighlightText() {
        return highlightNote.getText();
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;


        GuiCustomPartyFinder guiCustomPartyFinder = panelPartyFinder.getGuiCustomPartyFinder();
        if (guiCustomPartyFinder.getGuiChest() == null) return;
        Slot s = guiCustomPartyFinder.getGuiChest().inventorySlots.getSlot(9*5+5);
        ItemStack itemStack = s.getStack();
        if (itemStack == null) return;

        String dungeon="", floor="", text="";
        {
            NBTTagCompound stackTagCompound = itemStack.getTagCompound();
            if (stackTagCompound.hasKey("display", 10)) {
                NBTTagCompound nbttagcompound = stackTagCompound.getCompoundTag("display");

                if (nbttagcompound.getTagId("Lore") == 9) {
                    NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);

                    for (int i = 0; i < nbttaglist1.tagCount(); i++) {
                        String str = nbttaglist1.getStringTagAt(i);
                        if (str.startsWith("§aDungeon: ")) {
                            dungeon = str.substring(11);
                        } else if (str.startsWith("§aFloor: ")) {
                            floor = str.substring(9);
                        } else if (str.startsWith("§aSearch text: ")) {
                            text = str.substring(15);
                        }
                    }
                }
            }
        }
        fontRenderer.drawString("§aSearching: "+dungeon+" §7- "+floor, 5,155,-1);
        fontRenderer.drawString("§aSearch text: "+text, 5,155+fontRenderer.FONT_HEIGHT,-1);

        Gui.drawRect(0,160+fontRenderer.FONT_HEIGHT*2,getBounds().width, 161+fontRenderer.FONT_HEIGHT*2, -1);
        GlStateManager.translate(5,165+fontRenderer.FONT_HEIGHT*2,0);

        s = guiCustomPartyFinder.getGuiChest().inventorySlots.getSlot(9*5+8);
        itemStack = s.getStack();
        if (itemStack == null || itemStack.getItem() != Items.skull) return;

        List<String> list = itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
        for (int i = 0; i < list.size(); ++i) {
            if (i == 0) {
                list.set(i, itemStack.getRarity().rarityColor + list.get(i));
            } else {
                list.set(i, EnumChatFormatting.GRAY + list.get(i));
            }
        }
        for (int i = 0; i < list.size(); i++) {
            fontRenderer.drawString(list.get(i), 0, (i)*fontRenderer.FONT_HEIGHT, -1);
        }
    }

    public void refresh() {
        GuiChest chest = panelPartyFinder.getGuiCustomPartyFinder().getGuiChest();
        Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, 9*5+1, 0, 0, Minecraft.getMinecraft().thePlayer);
    }
    public void settings() {
        GuiChest chest = panelPartyFinder.getGuiCustomPartyFinder().getGuiChest();
        Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId, 9*5+5, 0, 0, Minecraft.getMinecraft().thePlayer);
    }

    @Override
    public void onBoundsUpdate() {
        Dimension bounds = getEffectiveDimension();
        refresh.setBounds(new Rectangle(5,5,(bounds.width-10)/2,20));
        createNew.setBounds(new Rectangle(bounds.width/2,5,(bounds.width-10)/2,20));
        filterCantjoin.setBounds(new Rectangle(5,30,bounds.width-10,20));
        filterWhitelistNote.setBounds(new Rectangle(5,55,bounds.width-10,20));
        filterBlacklistNote.setBounds(new Rectangle(5,80,bounds.width-10,20));
        plaeHighlightNote.setBounds(new Rectangle(5,105,bounds.width-10,20));
        settings.setBounds(new Rectangle(5,130,bounds.width-10,20));
    }

    public boolean filter(ItemStack itemStack) {
        NBTTagCompound stackTagCompound = itemStack.getTagCompound();
        String note = "";
        if (stackTagCompound.hasKey("display", 10)) {
            NBTTagCompound nbttagcompound = stackTagCompound.getCompoundTag("display");

            if (nbttagcompound.getTagId("Lore") == 9) {
                NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);

                for (int i = 0; i < nbttaglist1.tagCount(); i++) {
                    String str = nbttaglist1.getStringTagAt(i);
                    if (str.startsWith("§cRequires ") && filterCantjoinButton.isEnabled()) return false;
                    if (str.startsWith("§7§7Note:")) {
                        note = str.substring(12);
                    }
                }
            }
        }

        if (!filterBlacklist.getText().isEmpty() && note.toLowerCase().contains(filterBlacklist.getText().toLowerCase())) return false;
        if (!filterWhitelist.getText().isEmpty() && !note.toLowerCase().contains(filterWhitelist.getText().toLowerCase())) return false;

        return true;
    }
}
