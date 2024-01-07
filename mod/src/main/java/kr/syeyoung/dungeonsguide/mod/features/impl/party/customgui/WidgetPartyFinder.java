/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatSubscriber;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.CategoryPageWidget;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.MainConfigWidget;
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.events.impl.WindowUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteTooltip.WidgetEnableAskToJoin;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteTooltip.WidgetInvite;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.ModalMessage;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Stream;

public class WidgetPartyFinder extends AnnotatedImportOnlyWidget {
    public WidgetPartyFinder() {
        super(new ResourceLocation("dungeonsguide:gui/features/partyFinder/custom_party_finder.gui"));
        filterUnjoinable.addOnUpdate(this::updateUnjoinable);
        filterUnjoinable.addOnUpdate((old,neu) -> {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        });
        whitelist.addOnUpdate(this::updateUnjoinable);
        blacklist.addOnUpdate(this::updateUnjoinable);
        highlight.addOnUpdate(this::updateUnjoinable);
        blacklistClass.addOnUpdate(this::updateUnjoinable);
        whitelist.addOnUpdate((old, neu) -> FeatureRegistry.PARTYKICKER_CUSTOM.setWhitelist(neu));
        blacklist.addOnUpdate((old, neu) -> FeatureRegistry.PARTYKICKER_CUSTOM.setBlacklist(neu));
        highlight.addOnUpdate((old, neu) -> FeatureRegistry.PARTYKICKER_CUSTOM.setHighlight(neu));
        blacklistClass.addOnUpdate((old, neu) -> FeatureRegistry.PARTYKICKER_CUSTOM.setBlacklistClass(neu));
    }

    @Bind(variableName = "prevVisible")
    public final BindableAttribute<String> prevVisible = new BindableAttribute<>(String.class, "true");
    @Bind(variableName = "pageNumber")
    public final BindableAttribute<String> pageNumber = new BindableAttribute<>(String.class, "Page 1");
    @Bind(variableName = "nextVisible")
    public final BindableAttribute<String> nextVisible = new BindableAttribute<>(String.class, "true");

    @Bind(variableName = "partyList")
    public final BindableAttribute<Column> column = new BindableAttribute(Column.class);
    @Bind(variableName = "isEmpty")
    public final BindableAttribute<String> isEmpty = new BindableAttribute(String.class, "false");


    @Bind(variableName = "partyButtons")
    public final BindableAttribute<String> partyButtons = new BindableAttribute<>(String.class, "create");

    @Bind(variableName = "filterUnjoinable")
    public final BindableAttribute<Boolean> filterUnjoinable = new BindableAttribute<>(Boolean.class, true);

    @Bind(variableName = "whitelist")
    public final BindableAttribute<String> whitelist = new BindableAttribute<>(String.class, FeatureRegistry.PARTYKICKER_CUSTOM.getWhitelist());
    @Bind(variableName = "blacklist")
    public final BindableAttribute<String> blacklist = new BindableAttribute<>(String.class, FeatureRegistry.PARTYKICKER_CUSTOM.getBlacklist());
    @Bind(variableName = "highlight")
    public final BindableAttribute<String> highlight = new BindableAttribute<>(String.class, FeatureRegistry.PARTYKICKER_CUSTOM.getHighlight());
    @Bind(variableName = "blacklistClass")
    public final BindableAttribute<String> blacklistClass = new BindableAttribute<>(String.class, FeatureRegistry.PARTYKICKER_CUSTOM.getBlacklistClass());


    @Bind(variableName = "searching")
    public final BindableAttribute<String> searching = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "searchtext")
    public final BindableAttribute<String> searchText = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "classdungeonlv")
    public final BindableAttribute<String> classDungeonLv = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "sort")
    public final BindableAttribute<String> sort = new BindableAttribute<>(String.class, "");


    private void setDelistable(boolean delistable) {
        partyButtons.setValue((PartyManager.INSTANCE.getPartyContext() != null && !PartyManager.INSTANCE.isLeader()) ? "leave" : (delistable ? "delist" : "create"));
    }

    @On(functionName = "goBack")
    public void goBack() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(9*5+3, 0, 0);
    }

    @On(functionName = "next")
    public void nextPage() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(9*2+8, 0, 0);
    }
    @On(functionName = "prev")
    public void prevPage() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(9*2, 0, 0);
    }

    @On(functionName = "refresh")
    public void refresh() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(9*5+1, 0, 0);
    }

    @On(functionName = "leave")
    public void leaveParty() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        ChatProcessor.INSTANCE.subscribe(new ChatSubscriber() {
            int cnt = 0;
            @Override
            public ChatProcessResult process(String txt, Map<String, Object> context) {
                cnt++;
                if ("notinparty".equals(context.get("type"))) {
                    setDelistable(false);
                    return ChatProcessResult.REMOVE_LISTENER;
                }
                if (cnt == 10) {
                    return ChatProcessResult.REMOVE_LISTENER;
                }
                return ChatProcessResult.NONE;
            }
        });
        ChatProcessor.INSTANCE.addToChatQueue("/p leave ", () -> {}, true);
    }
    @On(functionName = "create")
    public void createParty() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(9*5+0, 0, 0);
    }
    @On(functionName = "delist")
    public void delistParty() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(9*5+7, 0, 0);
    }
    @On(functionName = "searchSettings")
    public void openSearchSettings() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(9*5+5, 0, 0);
    }


    @On(functionName = "invite")
    public void openInviteDialog() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        if (!DiscordIntegrationManager.INSTANCE.isLoaded()) {
            ModalMessage modalMessage = new ModalMessage("Discord GameSDK has been disabled, or it failed to load");
            PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Error", modalMessage, true), (a) -> {});
        } else if (PartyManager.INSTANCE.getAskToJoinSecret() != null) {
            WidgetInvite modalMessage = new WidgetInvite();
            PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Invite Discord Friend", modalMessage, true), (a) -> {});
        } else {
            WidgetEnableAskToJoin modalMessage = new WidgetEnableAskToJoin();
            PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Error", modalMessage, true), (a) -> {
                if (a != null)
                    openInviteDialog();
            });
        }
    }
    @On(functionName = "settings")
    public void openSettings() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        MainConfigWidget mainConfigWidget = new MainConfigWidget();
        GuiScreenAdapter adapter = new GuiScreenAdapter(new GlobalHUDScale(mainConfigWidget));
        Minecraft.getMinecraft().displayGuiScreen(adapter);

        Navigator.getNavigator(mainConfigWidget.getDomElement()).openPage(
                new CategoryPageWidget("Dungeon Party")
        );
    }


    private final Map<Integer, WidgetPartyElement> partyElementMap = new HashMap<>();
    public void onChestUpdate(WindowUpdateEvent windowUpdateEvent) {
        if (windowUpdateEvent == null) {
            GuiChest guiChest = GuiScreenAdapterChestOverride.getAdapter(getDomElement()).getGuiChest();
            if (guiChest == null) {
                partyElementMap.clear();
            } else {
                for (int x = 1; x<=7; x++) {
                    for (int y = 1; y <= 3; y++) {
                        int i = y * 9 + x;
                        Slot s = guiChest.inventorySlots.getSlot(i);
                        WidgetPartyElement prev = partyElementMap.remove(i);
                        if (s == null || !s.getHasStack()) { continue; }
                        if (!filter(s.getStack())) continue;

                        if (prev == null) prev = new WidgetPartyElement(this, i);
                        prev.update(PartyFinderParty.fromItemStack(s.getStack()));
                        partyElementMap.put(i, prev);
                    }
                }

                {
                    Slot next = guiChest.inventorySlots.getSlot(9 * 2 + 8);
                    if (next.getStack() != null && next.getStack().getItem() == Items.arrow) {
                        nextVisible.setValue("true");
                        extractPage(next.getStack());
                    } else {
                        nextVisible.setValue("false");
                    }

                    Slot prev = guiChest.inventorySlots.getSlot(9 * 2 + 0);
                    if (prev.getStack() != null && prev.getStack().getItem() == Items.arrow) {
                        prevVisible.setValue("true");
                        extractPage(prev.getStack());
                    } else {
                        prevVisible.setValue("false");
                    }

                    Slot delist = guiChest.inventorySlots.getSlot(9 * 5 + 7);
                    setDelistable(delist.getStack() != null && delist.getStack().getItem() == Item.getItemFromBlock(Blocks.bookshelf));

                    Slot search = guiChest.inventorySlots.getSlot(9*5+5);
                    if (search != null && search.getStack() != null)
                        extractSearch(search.getStack());
                }
            }
        } else {
            if (windowUpdateEvent.getPacketSetSlot() != null) {
                int i = windowUpdateEvent.getPacketSetSlot().func_149173_d();

                ItemStack stack = windowUpdateEvent.getPacketSetSlot().func_149174_e();
                if (i == 9*2+8) {
                    if (stack != null && stack.getItem() == Items.arrow) {
                        nextVisible.setValue("true");
                        extractPage(stack);
                    } else {
                        nextVisible.setValue("false");
                    }
                } else if (i == 9*2) {
                    if (stack != null && stack.getItem() == Items.arrow) {
                        prevVisible.setValue("true");
                        extractPage(stack);
                    } else {
                        prevVisible.setValue("false");
                    }
                } else if (i == 9*5+7) {
                    setDelistable(stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.bookshelf));
                } else if (i == 9*5 + 5) {
                    if (stack != null) {
                        extractSearch(stack);
                    }
                }

                if (i%9 == 0 || i%9 == 8 || i/9 == 0 || i/9 >= 4) {
                    return;
                }

                WidgetPartyElement prev = partyElementMap.remove(i);
                if (filter(stack)) {
                    if (prev == null) prev = new WidgetPartyElement(this, i);
                    prev.update(PartyFinderParty.fromItemStack(stack));
                    partyElementMap.put(i, prev);
                }
            } else if (windowUpdateEvent.getWindowItems() != null) {
                for (int x = 1; x<=7; x++) {
                    for (int y = 1; y <= 3; y++) {
                        int i = y * 9 + x;
                        ItemStack item = windowUpdateEvent.getWindowItems().getItemStacks()[i];
                        WidgetPartyElement prev = partyElementMap.remove(i);
                        if (!filter(item)) continue;

                        if (prev == null) prev = new WidgetPartyElement(this, i);
                        prev.update(PartyFinderParty.fromItemStack(item));
                        partyElementMap.put(i, prev);
                    }
                }

                {
                    ItemStack next = windowUpdateEvent.getWindowItems().getItemStacks()[9 * 2 + 8];
                    if (next != null && next.getItem() == Items.arrow) {
                        nextVisible.setValue("true");
                        extractPage(next);
                    } else {
                        nextVisible.setValue("false");
                    }

                    ItemStack prev = windowUpdateEvent.getWindowItems().getItemStacks()[9 * 2];
                    if (prev != null && prev.getItem() == Items.arrow) {
                        prevVisible.setValue("true");
                        extractPage(prev);
                    } else {
                        prevVisible.setValue("false");
                    }

                    ItemStack delist = windowUpdateEvent.getWindowItems().getItemStacks()[9*5+7];
                    setDelistable(delist != null && delist.getItem() == Item.getItemFromBlock(Blocks.bookshelf));

                    ItemStack search = windowUpdateEvent.getWindowItems().getItemStacks()[9*5+5];
                    if (search != null) extractSearch(search);
                }
            }
        }

        addItems();
    }
    public void extractPage(ItemStack itemStack) {
        NBTTagCompound stackTagCompound = itemStack.getTagCompound();
        int page = -1;
        if (stackTagCompound.hasKey("display", 10)) {
            NBTTagCompound nbtTagCompound = stackTagCompound.getCompoundTag("display");

            if (nbtTagCompound.getTagId("Lore") == 9) {
                NBTTagList nbtTagList1 = nbtTagCompound.getTagList("Lore", 8);

                for (int i = 0; i < nbtTagList1.tagCount(); i++) {
                    String str = nbtTagList1.getStringTagAt(i);
                    if (str.startsWith("§ePage ")) {
                        int pg = Integer.parseInt(str.substring(7));
                        if (itemStack.getDisplayName().equals("§aPrevious Page")) page = pg+1;
                        else page = pg-1;
                    }
                }
            }
        }
        pageNumber.setValue("Page "+page);
    }

    public void extractSearch(ItemStack itemStack) {
        String dungeon="", floor="", text="", classLv="", dungeonLv="", sort="";
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
                        } else if (str.startsWith("§aClass Level: ")) {
                            classLv = str.substring(15);
                        } else if (str.startsWith("§aDungeon Level: ")) {
                            dungeonLv = str.substring(17);
                        } else if (str.startsWith("§aSort: ")) {
                            sort =  str.substring(8);
                        }
                    }
                }
            }
        }


        searching.setValue("§aSearching: "+dungeon+" §7- "+floor);
        searchText.setValue("§aSearch text: "+text);
        classDungeonLv.setValue("§aClass/Dungeon Lv: "+classLv+"§7/"+dungeonLv);
        this.sort.setValue("§aSort: "+sort);
    }

    public boolean filter(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getItem() == null) return false;
        if (itemStack.getItem() != Items.skull) return false;

        PartyFinderParty party = PartyFinderParty.fromItemStack(itemStack);

        if (!party.canJoin && filterUnjoinable.getValue()) return false;

        Set<String> invalidClasses = new HashSet<>();
        for (String s : blacklistClass.getValue().split(",")) {
            invalidClasses.add(s.toLowerCase().trim());
        }
        for(String badClass : invalidClasses) {
            if(party.members.stream().anyMatch(a -> a.getClazz().equalsIgnoreCase(badClass)))return false;
        }
        if (!blacklist.getValue().isEmpty()) {
            for (String s1 : blacklist.getValue().split(",")) {
                if (party.note.toLowerCase().contains(s1.toLowerCase())) return false;
            }
        }
        if (!whitelist.getValue().isEmpty()) {
            boolean s = false;
            for (String s1 : whitelist.getValue().split(",")) {
                if (party.note.toLowerCase().contains(s1.toLowerCase())) {
                    s = true; break;
                }
            }
            if (!s) return false;
        }
        return true;
    }

    public void addItems() {
        isEmpty.setValue(partyElementMap.size() == 0 ? "true" : "false");
        column.getValue().removeAllWidget();
        Stream<WidgetPartyElement> widgets = partyElementMap.values().stream().sorted(
                Comparator
                        .<WidgetPartyElement>comparingInt(a -> Math.max(a.getParty().requiredDungeonLevel, a.getParty().requiredClassLevel))
                        .reversed()
        );
        widgets.forEach(column.getValue()::addWidget);
    }

    public void updateUnjoinable(Object prev, Object neu) {
        onChestUpdate(null);
    }
}
