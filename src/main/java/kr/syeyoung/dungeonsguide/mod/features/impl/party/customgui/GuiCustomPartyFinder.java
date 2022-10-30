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

import kr.syeyoung.dungeonsguide.mod.events.impl.WindowUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.gui.MGui;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;

import java.awt.*;

public class GuiCustomPartyFinder extends MGui {
    @Getter
    private GuiChest guiChest;

    public void setGuiChest(GuiChest guiChest) {
        if (this.guiChest != null) this.guiChest.onGuiClosed();
        this.guiChest = guiChest;
        panelPartyFinder.onChestUpdate(null);
        guiChest.setWorldAndResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        guiChest.initGui();
    }

    public void onChestUpdate(WindowUpdateEvent windowUpdateEvent) {
        panelPartyFinder.onChestUpdate(windowUpdateEvent);
    }

    private PanelPartyFinder panelPartyFinder;
    public GuiCustomPartyFinder() {
        panelPartyFinder = new PanelPartyFinder(this);
        getMainPanel().add(panelPartyFinder);
    }

    @Override
    public void initGui() {
        super.initGui();
        int width  = 3*Minecraft.getMinecraft().displayWidth/5;
        width = Math.max(width, 1000);
        int height = 3*Minecraft.getMinecraft().displayHeight/5;
        height = Math.max(height, 600);

        panelPartyFinder.setBounds(new Rectangle((Minecraft.getMinecraft().displayWidth-width)/2, (Minecraft.getMinecraft().displayHeight-height)/2, width, height));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

    }

    @Override
    public void onGuiClosed() {
        guiChest.onGuiClosed();
        guiChest = null;
        super.onGuiClosed();
    }
}
