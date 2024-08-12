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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;



import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.WindowUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui.WidgetPartyFinder;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Scaler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL30;

public class FeatureCustomLeapGui extends SimpleFeature {
    public FeatureCustomLeapGui() {
        super("Dungeon HUD","Custom Spirit Leap","Custom Spirit Leap UI with great enhancements", "dungeon.customleap", true);
    }

    @Getter @Setter
    private String lastClass = "";

    private WidgetSpiritLeap widgetSpiritLeap;
    private GuiScreenAdapterChestOverride guiScreenAdapter;

    private String conditionCheck(GuiScreen guiScreen) {
        if (!(guiScreen instanceof GuiChest)) return null;
        GuiChest chest = (GuiChest) guiScreen;
        if (!(chest.inventorySlots instanceof ContainerChest)) return null;
        ContainerChest containerChest = (ContainerChest) chest.inventorySlots;
        IInventory lower = containerChest.getLowerChestInventory();
        if (lower == null) return null;
        return lower.getName();
    }

    @DGEventHandler
    public void onGuiOpen(GuiOpenEvent event) {
        String name = conditionCheck(event.gui);
        if (name == null) {
            widgetSpiritLeap = null;
            guiScreenAdapter = null;
            return;
        }
        if (!name.equals("Spirit Leap")) {
            if (guiScreenAdapter != null) {
                guiScreenAdapter.setCanExitWithoutClosing(true);
                widgetSpiritLeap = null;
                guiScreenAdapter = null;
            }
            return;
        }

        if (widgetSpiritLeap == null || guiScreenAdapter == null) {
            widgetSpiritLeap = new WidgetSpiritLeap();

            Scaler scaler = new Scaler();
            scaler.scale.setValue((double) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
            scaler.child.setValue(widgetSpiritLeap);
            int x = (int) (Minecraft.getMinecraft().displayWidth / 2 - 300 * scaler.scale.getValue() + 50);
            int y = (int) (Minecraft.getMinecraft().displayHeight / 2 - 200 * scaler.scale.getValue() + 100);
            guiScreenAdapter = new GuiScreenAdapterChestOverride(scaler, x, y);
        }
        guiScreenAdapter.setGuiChest((GuiChest) event.gui);

        event.gui = guiScreenAdapter;
    }

    @DGEventHandler
    public void onGuiUpdate(WindowUpdateEvent windowUpdateEvent) {
        if (widgetSpiritLeap != null) {
            widgetSpiritLeap.onChestUpdate(windowUpdateEvent);
        }
    }
}
