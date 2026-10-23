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

import java.util.List;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenOpenEvent;


import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.WindowUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Scaler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class FeatureCustomPartyFinder extends SimpleFeature {
    public FeatureCustomPartyFinder() {
        super("Dungeon Party","Custom Party Finder","Custom Party Finder", "party.customfinder", true);
    }

    @Getter
    @Setter
    private String whitelist = "", blacklist = "", highlight ="", blacklistClass = "", highlightClass = "";

    @Override
    public JsonObject saveConfig() {
        JsonObject jsonObject = super.saveConfig();
        jsonObject.addProperty("whitelist", whitelist);
        jsonObject.addProperty("blacklist", blacklist);
        jsonObject.addProperty("highlight", highlight);
        jsonObject.addProperty("blacklistClass", blacklistClass);
        jsonObject.addProperty("highlightClass", highlightClass);
        return jsonObject;
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        this.whitelist = jsonObject.has("whitelist") ? jsonObject.get("whitelist").getAsString() : "";
        this.blacklist = jsonObject.has("blacklist") ? jsonObject.get("blacklist").getAsString() : "";
        this.highlight = jsonObject.has("highlight") ? jsonObject.get("highlight").getAsString() : "";
        this.blacklistClass = jsonObject.has("blacklistClass") ? jsonObject.get("blacklistClass").getAsString() : "";
        this.highlightClass = jsonObject.has("highlightClass") ? jsonObject.get("highlightClass").getAsString() : "";
    }

    @Getter @Setter
    private String lastClass = "";

//    GuiCustomPartyFinder guiCustomPartyFinder;
    private WidgetPartyFinder widgetPartyFinder;
    private GuiScreenAdapterChestOverride guiScreenAdapter;

    private String conditionCheck(GuiScreen guiScreen) {
        if (!(guiScreen instanceof ContainerScreen)) return null;
        ContainerScreen chest = (ContainerScreen) guiScreen;
        if (!(chest.getMenu() instanceof ChestMenu)) return null;
        ChestMenu containerChest = (ChestMenu) chest.getMenu();
        Container lower = containerChest.getContainer();
        if (lower == null) return null;
        // 1.8.9's chest inventory name is the screen title on 26.1 (D125)
        return chest.getTitle().getString();
    }

    @DGEventHandler
    public void onGuiOpen(ScreenOpenEvent event) {
        String name = conditionCheck(event.gui);
        if (name == null) {
            widgetPartyFinder = null;
            guiScreenAdapter = null;
            return;
        }
        if (!name.equals("Party Finder")) {
            if (guiScreenAdapter != null) {
                guiScreenAdapter.setCanExitWithoutClosing(true);
                widgetPartyFinder = null;
                guiScreenAdapter = null;
            }
            return;
        }

        if (widgetPartyFinder == null || guiScreenAdapter == null) {
            widgetPartyFinder = new WidgetPartyFinder();

            Scaler scaler = new Scaler();
            scaler.scale.setValue((double) new ScaledResolution(Minecraft.getInstance()).getScaleFactor());
            scaler.child.setValue(widgetPartyFinder);
            guiScreenAdapter = new GuiScreenAdapterChestOverride(scaler);
        }
        guiScreenAdapter.setGuiChest((ContainerScreen) event.gui);

        event.gui = guiScreenAdapter;
    }

    @DGEventHandler
    public void onGuiUpdate(WindowUpdateEvent windowUpdateEvent) {
        if (widgetPartyFinder != null) {
            widgetPartyFinder.onChestUpdate(windowUpdateEvent);
        }

        if (Minecraft.getInstance().screen instanceof ContainerScreen) {
            ContainerScreen chest = (ContainerScreen) Minecraft.getInstance().screen;

            if (!(chest.getMenu() instanceof ChestMenu)) return;
            ChestMenu containerChest = (ChestMenu) chest.getMenu();
            Container lower = containerChest.getContainer();
            if (lower == null || !chest.getTitle().getString().equals("Catacombs Gate")) return;

            ItemStack item = null;
            if (windowUpdateEvent.getWindowItems() != null) {
                if (windowUpdateEvent.getWindowItems().items().size() <= 47) return;
                item = windowUpdateEvent.getWindowItems().items().get(47);

            } else if (windowUpdateEvent.getPacketSetSlot() != null) {
                if (windowUpdateEvent.getPacketSetSlot().getSlot() != 47) return;
                item = windowUpdateEvent.getPacketSetSlot().getItem();
            }
            if (item.isEmpty()) return;

            // 1.8.9's display.Lore, the lore component on 26.1 (D125)
            List<String> nbtTagList1 = TextUtils.getFormattedLore(item);

            for (int i = 0; i < nbtTagList1.size(); i++) {
                String str = nbtTagList1.get(i);
                if (str.startsWith("§aCurrently Selected")) {
                    lastClass = str.substring(24);
                }
            }

        }
    }
}
