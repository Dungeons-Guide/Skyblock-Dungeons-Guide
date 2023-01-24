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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig;

import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class MCategoryElement extends MPanel {
    private String category;
    private Runnable onClick;
    private int leftPad = 0;
    private int offsetX;
    private RootConfigPanel rootConfigPanel;
    public MCategoryElement(String category, Runnable onClick, int leftPad, int offsetX, RootConfigPanel root) {
        this.category = category;
        this.onClick = onClick;
        this.leftPad = leftPad;
        this.offsetX = offsetX;
        this.rootConfigPanel = root;
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (rootConfigPanel.getCurrentPage().equals(category)) {
            clip(0,scissor.y, Minecraft.getMinecraft().displayWidth, scissor.height);
            Gui.drawRect(leftPad - offsetX, 0, getBounds().width, getBounds().height, RenderUtils.blendAlpha(0x141414, 0.13f));
        } else if (lastAbsClip.contains(absMousex, absMousey) && getTooltipsOpen() == 0) {
            clip(0,scissor.y, Minecraft.getMinecraft().displayWidth, scissor.height);
            Gui.drawRect(leftPad - offsetX, 0, getBounds().width, getBounds().height, RenderUtils.blendAlpha(0x141414, 0.09f));
        }
        clip(scissor.x, scissor.y, scissor.width, scissor.height);


        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        String name = category.substring(category.lastIndexOf(".")+1);
        fr.drawString(name, leftPad,2,-1);

    }

    @Override
    public Dimension getPreferredSize() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        return new Dimension(fr.getStringWidth(category.substring(category.lastIndexOf(".")+1)) + leftPad+10, fr.FONT_HEIGHT+4);
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (!lastAbsClip.contains(absMouseX, absMouseY) || getTooltipsOpen() > 0) { return; }
        if (onClick != null) onClick.run();
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

    }
    @Override
    public void mouseMoved(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {
        if (lastAbsClip.contains(absMouseX, absMouseY))
            setCursor(EnumCursor.POINTING_HAND);
    }
}
