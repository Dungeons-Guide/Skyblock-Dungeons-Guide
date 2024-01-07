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



import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.WindowUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Scaler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiOpenEvent;

public class FeatureCustomPartyFinder extends SimpleFeature {
    public FeatureCustomPartyFinder() {
        super("Dungeon Party","Custom Party Finder","Custom Party Finder", "party.customfinder", true);
    }

    @Getter
    @Setter
    private String whitelist = "", blacklist = "", highlight ="", blacklistClass = "";

    @Getter @Setter
    private String lastClass = "";

//    GuiCustomPartyFinder guiCustomPartyFinder;
    private WidgetPartyFinder widgetPartyFinder;
    private GuiScreenAdapterChestOverride guiScreenAdapter;
    @DGEventHandler
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null) {
            widgetPartyFinder = null;
            guiScreenAdapter = null;
        }
        
        if (!(event.gui instanceof GuiChest)) return;
        GuiChest chest = (GuiChest) event.gui;
        if (!(chest.inventorySlots instanceof ContainerChest)) return;
        ContainerChest containerChest = (ContainerChest) chest.inventorySlots;
        IInventory lower = containerChest.getLowerChestInventory();
        if (lower == null || !lower.getName().equals("Party Finder")) {
            if (guiScreenAdapter != null) {
                guiScreenAdapter.setCanExitWithoutClosing(true);
            }
            return;
        }

        if (widgetPartyFinder == null || guiScreenAdapter == null) {
            widgetPartyFinder = new WidgetPartyFinder();

            Scaler scaler = new Scaler();
            scaler.scale.setValue((double) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
            scaler.child.setValue(widgetPartyFinder);
            guiScreenAdapter = new GuiScreenAdapterChestOverride(scaler);
        }
        guiScreenAdapter.setGuiChest((GuiChest) event.gui);

        event.gui = guiScreenAdapter;
    }

    @DGEventHandler
    public void onGuiUpdate(WindowUpdateEvent windowUpdateEvent) {
        if (widgetPartyFinder != null) {
            widgetPartyFinder.onChestUpdate(windowUpdateEvent);
        }

        if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
            GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;

            if (!(chest.inventorySlots instanceof ContainerChest)) return;
            ContainerChest containerChest = (ContainerChest) chest.inventorySlots;
            IInventory lower = containerChest.getLowerChestInventory();
            if (lower == null || !lower.getName().equals("Catacombs Gate")) return;

            ItemStack item = null;
            if (windowUpdateEvent.getWindowItems() != null) {
                item = windowUpdateEvent.getWindowItems().getItemStacks()[47];
            } else if (windowUpdateEvent.getPacketSetSlot() != null) {
                if (windowUpdateEvent.getPacketSetSlot().func_149173_d() != 47) return;
                item = windowUpdateEvent.getPacketSetSlot().func_149174_e();
            }
            if (item == null) return;

            NBTTagCompound stackTagCompound = item.getTagCompound();
            if (stackTagCompound.hasKey("display", 10)) {
                NBTTagCompound nbtTagCompound = stackTagCompound.getCompoundTag("display");

                if (nbtTagCompound.getTagId("Lore") == 9) {
                    NBTTagList nbtTagList1 = nbtTagCompound.getTagList("Lore", 8);

                    for (int i = 0; i < nbtTagList1.tagCount(); i++) {
                        String str = nbtTagList1.getStringTagAt(i);
                        if (str.startsWith("§aCurrently Selected")) {
                            lastClass = str.substring(24);
                        }
                    }
                }
            }
        }
    }
}
