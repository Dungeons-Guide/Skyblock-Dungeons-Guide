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


import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.gui.CustomGuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Scaler;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.event.events.WindowUpdateEvent;
import kr.syeyoung.modapi.gui.UContainerChest;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiOpenEvent;

public class FeatureCustomLeapGui extends SimpleFeature {
    public FeatureCustomLeapGui() {
        super("Dungeon HUD","Custom Spirit Leap","Custom Spirit Leap UI with great enhancements", "dungeon.customleap", true);
    }

    @Getter @Setter
    private String lastClass = "";

    private WidgetSpiritLeap widgetSpiritLeap;
    private CustomGuiScreenAdapterChestOverride guiScreenAdapter;

    @DGEventHandler
    public void onGuiOpen(GuiOpenEvent event) {
        UContainerChest container = ModAPI.getAPI().extractContainerChest(event.gui);
        if (container == null || !"Spirit Leap".equals((container).getName())) {
            if (guiScreenAdapter != null) {
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
            int x = (int) (Math.max(0, ModAPI.getAPI().getDisplayWidth() / 2 - 200 * scaler.scale.getValue()));
            int y = (int) (Math.max(0, ModAPI.getAPI().getDisplayHeight() / 2 - 200 * scaler.scale.getValue()) + 100);
            guiScreenAdapter = new CustomGuiScreenAdapterChestOverride(scaler, x, y);
        }
        guiScreenAdapter.setGuiChest(container);

        event.gui = guiScreenAdapter;
    }

    @DGEventHandler
    public void onGuiUpdate(WindowUpdateEvent windowUpdateEvent) {
        if (widgetSpiritLeap != null) {
            widgetSpiritLeap.onChestUpdate(windowUpdateEvent);
        }
    }
}
