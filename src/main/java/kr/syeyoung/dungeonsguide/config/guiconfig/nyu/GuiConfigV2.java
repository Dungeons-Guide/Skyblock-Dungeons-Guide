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

import kr.syeyoung.dungeonsguide.config.guiconfig.old.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.gui.MGui;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class GuiConfigV2 extends MGui {

    private RootConfigPanel rootConfigPanel;

    public GuiConfigV2() {
        rootConfigPanel = new RootConfigPanel();
        rootConfigPanel.setPageGenerator(ConfigPanelCreator.INSTANCE);
        getMainPanel().add(rootConfigPanel);
    }


    @Override
    public void initGui() {
        super.initGui();
        int dw = Minecraft.getMinecraft().displayWidth;
        int dh = Minecraft.getMinecraft().displayHeight;
        rootConfigPanel.setBounds(new Rectangle((dw-1000)/2, (dh-800)/2, 1000,800));
        rootConfigPanel.setScale(2.0f);
    }
}
