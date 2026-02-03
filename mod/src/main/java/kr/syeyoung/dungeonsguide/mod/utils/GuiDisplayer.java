/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.utils;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.event.ListenerPriority;
import kr.syeyoung.modapi.event.SubscribeEvent;
import kr.syeyoung.modapi.event.events.GuiOpenEvent;
import kr.syeyoung.modapi.gui.UGuiScreen;

import java.util.LinkedList;
import java.util.Queue;

public class GuiDisplayer {
    public static GuiDisplayer INSTANCE = new GuiDisplayer();
    private GuiDisplayer() {}

    private Queue<UGuiScreen> guiScreensToShow = new LinkedList<>();
    private boolean isMcLoaded;

    @SubscribeEvent(priority = ListenerPriority.LAST)
    public void onGuiOpen(GuiOpenEvent guiOpenEvent) {
        isMcLoaded = true;
        if (ModAPI.getAPI().getCurrentGuiScreen() == guiScreensToShow.peek()) {
            guiScreensToShow.poll();
        }
        if (!guiScreensToShow.isEmpty()) {
            UGuiScreen gui = guiScreensToShow.peek();
            if (gui == null) return;
            guiOpenEvent.setGui(gui);
        }
    }

    public void displayGui(UGuiScreen specialGuiScreen) {
        if (specialGuiScreen == null) return;
        if (!guiScreensToShow.contains(specialGuiScreen))
            guiScreensToShow.add(specialGuiScreen);
        if (isMcLoaded && ModAPI.getAPI().isCallingFromMinecraftThread()) {
            if (guiScreensToShow.size() == 1)
                ModAPI.getAPI().displayGuiScreen(guiScreensToShow.peek());
        } else if (isMcLoaded) {
            if (guiScreensToShow.size() == 1)
                DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
                    ModAPI.getAPI().displayGuiScreen(guiScreensToShow.peek());
                });
        }
    }
}
