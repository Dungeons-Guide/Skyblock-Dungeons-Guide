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


import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.elements.*;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.PartyFinderParty;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PanelPartyFinderSettings extends MPanelScaledGUI {
    private PanelPartyFinder panelPartyFinder;

    private MButton refresh = new MButton(), createNew = new MButton(), settings = new MButton();
    private MPassiveLabelAndElement filterCantjoin, filterWhitelistNote, filterBlacklistNote, plaeHighlightNote, cataLv,blacklistClass; private MToggleButton filterCantjoinButton;
    private MTextField filterWhitelist, filterBlacklist, highlightNote, blacklistClassTxt;
    private MIntegerSelectionButton integerSelection;

    @Getter
    boolean delistable = false;

    public void setDelistable(boolean delistable) {
        this.delistable = delistable;
        updateCreateNew();
    }

    public void updateCreateNew() {
        createNew.setText((PartyManager.INSTANCE.getPartyContext() != null && !PartyManager.INSTANCE.isLeader()) ? "Leave Party" : (delistable ? "De-list" : "Create New"));
    }

    public PanelPartyFinderSettings(PanelPartyFinder panelPartyFinder) {
        this.panelPartyFinder = panelPartyFinder;

        createNew.setOnActionPerformed(this::createNew);
        createNew.setBackground(0xFF00838F);
        createNew.setHover(0xFF00ACC1);
        createNew.setClicked(0xFF0097A7);
        add(createNew);
        updateCreateNew();
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
            blacklistClassTxt = new MTextField() {
                @Override
                public void edit(String str) {
                    super.edit(str);
                    FeatureRegistry.PARTYKICKER_CUSTOM.setBlacklistClass(str);
                    panelPartyFinder.onChestUpdate(null);
                }
            };

            filterWhitelist.setText(FeatureRegistry.PARTYKICKER_CUSTOM.getWhitelist());
            filterBlacklist.setText(FeatureRegistry.PARTYKICKER_CUSTOM.getBlacklist());
            highlightNote.setText(FeatureRegistry.PARTYKICKER_CUSTOM.getHighlight());
            blacklistClassTxt.setText(FeatureRegistry.PARTYKICKER_CUSTOM.getBlacklistClass());

            filterWhitelistNote = new MPassiveLabelAndElement("Whitelist Note", filterWhitelist);
            filterBlacklistNote = new MPassiveLabelAndElement("Blacklist Note", filterBlacklist);
            plaeHighlightNote = new MPassiveLabelAndElement("Highlight Note", highlightNote);
            blacklistClass = new MPassiveLabelAndElement("Blacklist Class", blacklistClassTxt);

            filterWhitelistNote.setDivideRatio(0.5);
            filterBlacklistNote.setDivideRatio(0.5);
            plaeHighlightNote.setDivideRatio(0.5);
            blacklistClass.setDivideRatio(0.5);
            add(filterWhitelistNote);
            add(filterBlacklistNote);
            add(plaeHighlightNote);
            add(blacklistClass);
        }
        {
            integerSelection = new MIntegerSelectionButton(FeatureRegistry.PARTYKICKER_CUSTOM.getMinimumCata());
            integerSelection.setOnUpdate(() -> {
                FeatureRegistry.PARTYKICKER_CUSTOM.setMinimumCata(integerSelection.getData());
                panelPartyFinder.onChestUpdate(null);
            });
            cataLv = new MPassiveLabelAndElement("Minimum Cata Lv", integerSelection);
            cataLv.setDivideRatio(0.5); add(cataLv);
        }
    }

    private void createNew() {
        if (PartyManager.INSTANCE.getPartyContext() != null && !PartyManager.INSTANCE.isLeader()) {
            ChatProcessor.INSTANCE.addToChatQueue("/p leave ", () -> {}, true);
            return;
        }
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
                NBTTagCompound nbtTagCompound = stackTagCompound.getCompoundTag("display");

                if (nbtTagCompound.getTagId("Lore") == 9) {
                    NBTTagList nbtTagList1 = nbtTagCompound.getTagList("Lore", 8);

                    for (int i = 0; i < nbtTagList1.tagCount(); i++) {
                        String str = nbtTagList1.getStringTagAt(i);
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
        refresh.setBounds(new Rectangle(5,5,(bounds.width-10)/2,15));
        createNew.setBounds(new Rectangle(bounds.width/2,5,(bounds.width-10)/2,15));
        filterCantjoin.setBounds(new Rectangle(5,22,bounds.width-10,15));
        filterWhitelistNote.setBounds(new Rectangle(5,39,bounds.width-10,15));
        filterBlacklistNote.setBounds(new Rectangle(5,56,bounds.width-10,15));
        plaeHighlightNote.setBounds(new Rectangle(5,73,bounds.width-10,15));
        cataLv.setBounds(new Rectangle(5,90,bounds.width-10,15));
        blacklistClass.setBounds(new Rectangle(5,107,bounds.width-10,15));
        settings.setBounds(new Rectangle(5,124,bounds.width-10,15));
    }

    public boolean filter(ItemStack itemStack) {
        PartyFinderParty party = PartyFinderParty.fromItemStack(itemStack);
        
        Set<String> invalidClasses = new HashSet<>();
        for (String s : blacklistClassTxt.getText().split(",")) {
            invalidClasses.add(s.toLowerCase().trim());
        }
        for(String badClass : invalidClasses) {
            if(party.classes.contains(badClass))return false;
        }
        if (!party.canJoin && filterCantjoinButton.isEnabled()) return false;
        if (integerSelection.getData() > party.requiredDungeonLevel) return false;

        if (!filterBlacklist.getText().isEmpty()) {
            for (String s1 : filterBlacklist.getText().split(",")) {
                if (party.note.toLowerCase().contains(s1.toLowerCase())) return false;
            }
        }
        if (!filterWhitelist.getText().isEmpty()) {
            boolean s = false;
            for (String s1 : filterWhitelist.getText().split(",")) {
                if (party.note.toLowerCase().contains(s1.toLowerCase())) {
                    s = true; break;
                }
            }
            if (!s) return false;
        }
        return true;
    }
}
