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

package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class MPopupMenu extends MTooltip {
    public MPopupMenu(int x, int y, List<MPanel> popupMenuElementList) {
        int maxWidth = 150;
        for (MPanel mPanel : popupMenuElementList) {
            Dimension dimension = mPanel.getPreferredSize();
            if (dimension.width > maxWidth) maxWidth = dimension.width;
        }
        int h1 = 7;
        for (MPanel mPanel : popupMenuElementList) {
            Dimension dimension = mPanel.getPreferredSize();
            mPanel.setBounds(new Rectangle(7,h1, maxWidth-13, dimension.height));
            h1 += dimension.height + 7;
            add(mPanel);
        }

        if (y + h1 > Minecraft.getMinecraft().displayHeight)
            y = Minecraft.getMinecraft().displayHeight - h1;
        if (x + maxWidth+ 2 > Minecraft.getMinecraft().displayWidth)
            x = Minecraft.getMinecraft().displayWidth - maxWidth-2;
        setBounds(new Rectangle(x,y,maxWidth+2, h1));
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        super.render(absMousex, absMousey, relMousex0, relMousey0, partialTicks, scissor);
        int radius = 7;
        double deltaDegree = Math.PI/6;
        RenderUtils.drawRoundedRectangle(0,0,getBounds().width,getBounds().height,radius,deltaDegree, RenderUtils.blendAlpha(0x121212, 0.0f));
        for (int i = 1; i < getChildComponents().size(); i++) {
            MPanel childComponent = getChildComponents().get(i);
            Gui.drawRect(7,childComponent.getBounds().y - 4, getBounds().width-7, childComponent.getBounds().y - 3, RenderUtils.blendAlpha(0x121212, 0.10f));
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (!getBounds().contains(absMouseX, absMouseY)) {
            close();
        }
    }
}
