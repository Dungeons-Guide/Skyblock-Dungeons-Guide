/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;

public class MLabel extends MPanel {
    @Getter
    @Setter
    private String text;

    @Getter
    @Setter
    private Color foreground = Color.white;

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }
    @Getter
    @Setter
    private Alignment alignment= Alignment.LEFT;

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        Dimension bounds = getSize();

        FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        int width = renderer.getStringWidth(getText());
        int x,y;
        if (alignment == Alignment.CENTER) {
            x = (getBounds().width - width) / 2;
             y = (getBounds().height - renderer.FONT_HEIGHT) / 2;
        } else if (alignment == Alignment.LEFT) {
             x = 0;
             y = (getBounds().height - renderer.FONT_HEIGHT) / 2;
        } else if (alignment == Alignment.RIGHT) {
            x = getBounds().width - width;
            y = (getBounds().height - renderer.FONT_HEIGHT) / 2;
        } else{
            return;
        }
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        renderer.drawString(getText(), x,y, 0xffffffff);
    }
}
