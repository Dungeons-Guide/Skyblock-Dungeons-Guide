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

package kr.syeyoung.dungeonsguide.config.guiconfig.nyu;

import kr.syeyoung.dungeonsguide.gui.MGui;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class GuiConfigV2 extends MGui {

    @Getter
    private RootConfigPanel rootConfigPanel;

    public GuiConfigV2() {
        rootConfigPanel = new RootConfigPanel(this);
        getMainPanel().add(rootConfigPanel);
    }


    @Override
    public void initGui() {
        super.initGui();
        int dw = Minecraft.getMinecraft().displayWidth;
        int dh = Minecraft.getMinecraft().displayHeight;
        int width = MathHelper.clamp_int(dw - 200, 1250, 1500), height = MathHelper.clamp_int(dh - 200, 600, 800);
        double scale = 2.0;
        if (dw <= width || dh <= height) {
            width = width/2; height = height/2;
            scale = 1.0;
        }
        rootConfigPanel.setBounds(new Rectangle((dw-width)/2, (dh-height)/2, width,height));
        rootConfigPanel.setScale(scale);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
