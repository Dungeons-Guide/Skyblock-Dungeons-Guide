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
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;

@Getter
@Setter
public class MToggleButton extends MPanel {
    private boolean enabled = true;
    private Runnable onToggle;

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        Dimension bounds = getSize();

        int gap = 1;

        Gui.drawRect(0, 0, bounds.width, bounds.height, 0xFF333333);
        Gui.drawRect(gap, gap, bounds.width-gap, bounds.height-gap, 0xFF171717);


        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = 1;
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (enabled) {
            int x = (int) ((scale * bounds.height - fr.FONT_HEIGHT)/2 + gap);
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0/scale,1.0/scale,0);
            fr.drawString("ON", x, x, 0xFF9B9B9B);
            GlStateManager.popMatrix();
            Gui.drawRect(bounds.width - bounds.height+gap,gap, bounds.width - gap, bounds.height - gap, 0xFF00B200);
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0/scale,1.0/scale,0);
            int x = (int) ((scale * bounds.height - fr.FONT_HEIGHT)/2 + gap);
            fr.drawString("OFF", (int) (scale * bounds.width - x - fr.getStringWidth("OFF")), x, 0xFF9B9B9B);
            GlStateManager.popMatrix();
            Gui.drawRect(gap,gap, bounds.height - gap, bounds.height - gap, 0xFFCD4000);
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (onToggle != null && lastAbsClip.contains(absMouseX, absMouseY)) {
            enabled = !enabled;
            onToggle.run();
        }
    }
}
