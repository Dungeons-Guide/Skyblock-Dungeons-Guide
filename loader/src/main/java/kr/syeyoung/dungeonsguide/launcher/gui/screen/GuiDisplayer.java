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

package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;
import java.util.Queue;

public class GuiDisplayer {
    public static GuiDisplayer INSTANCE = new GuiDisplayer();
    private GuiDisplayer() {}

    private Queue<GuiScreen> guiScreensToShow = new LinkedList<>();
    private boolean isMcLoaded;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent guiOpenEvent) {
        isMcLoaded = true;
        if (Minecraft.getMinecraft().currentScreen == guiScreensToShow.peek()) {
            guiScreensToShow.poll();
        }
        if (!guiScreensToShow.isEmpty()) {
            GuiScreen gui = guiScreensToShow.peek();
            if (gui == null) return;
            guiOpenEvent.gui = gui;
        }
    }

    public void displayGui(GuiScreen specialGuiScreen) {
        if (specialGuiScreen == null) return;
        if (!guiScreensToShow.contains(specialGuiScreen))
            guiScreensToShow.add(specialGuiScreen);
        if (isMcLoaded && Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            if (guiScreensToShow.size() == 1)
                Minecraft.getMinecraft().displayGuiScreen(guiScreensToShow.peek());
        } else if (isMcLoaded) {
            if (guiScreensToShow.size() == 1)
                Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(guiScreensToShow.peek()));
        }
    }
}
