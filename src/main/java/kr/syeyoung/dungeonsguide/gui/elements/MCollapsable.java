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
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Note it is passive.
 */
public class MCollapsable extends MPanel {
    @Getter
    @Setter
    private boolean collapsed = true;

    private MPanel representing;
    @Getter
    private MList lowerElements;

    @Getter @Setter
    private int leftPad = 0, leftPadElements = 13;

    private Runnable onPreferedSizeChange;

    public MCollapsable(MPanel representing, Runnable onPreferedSizeChange) {
        this.representing = representing;
        super.add(representing);
        lowerElements = new MList();
        lowerElements.setGap(0);
        super.add(lowerElements);

        this.onPreferedSizeChange = onPreferedSizeChange;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension rep = representing.getPreferredSize();
        if (collapsed) {
            return new Dimension(rep.width+leftPad+10, rep.height);
        } else {
            Dimension lowerElem = lowerElements.getPreferredSize();
            return new Dimension(Math.max(rep.width+leftPad+10, leftPadElements + lowerElem.width), rep.height+lowerElem.height+lowerElements.getGap());
        }
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        Dimension representingSize =this.representing.getPreferredSize();
        Dimension lowerSize = lowerElements.getPreferredSize();

        representing.setBounds(new Rectangle(new Point(leftPad+10, 0), new Dimension(bounds.width - (leftPad+10), representingSize.height)));
        lowerElements.setBounds(new Rectangle(new Point(leftPadElements, representingSize.height+lowerElements.getGap()), new Dimension(bounds.width - (leftPadElements), lowerSize.height)));
        lowerElements.realignChildren();
    }

    @Override
    public void render0(double scale, Point parentPoint, Rectangle parentClip, int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) {
        super.render0(scale, parentPoint, parentClip, absMousex, absMousey, relMousex0, relMousey0, partialTicks);

        clip(lastAbsClip.x, lastAbsClip.y, lastAbsClip.width, lastAbsClip.height);
//        GL11.glEnable(GL11.GL_SCISSOR_TEST);


        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        if (collapsed) {
            GlStateManager.translate(leftPad + 10 - fr.getStringWidth(">"),(bounds.height - fr.FONT_HEIGHT)/2,0);
        } else {
            GlStateManager.translate(leftPad + fr.FONT_HEIGHT,(representing.getPreferredSize().height - fr.getStringWidth(">"))/2,0);
            GlStateManager.rotate(90, 0,0,1);
        }

        fr.drawString(">", 0,0, -1);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void add(MPanel child) {
        lowerElements.add(child);
        if (onPreferedSizeChange != null) onPreferedSizeChange.run();
    }

    @Override
    public void remove(MPanel panel) {
        lowerElements.remove(panel);
        if (onPreferedSizeChange != null) onPreferedSizeChange.run();
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (relMouseX >= leftPad && relMouseY >= 0 && relMouseX <= leftPad + 10 && relMouseY <= representing.getPreferredSize().height) {
            collapsed = !collapsed;
            if (onPreferedSizeChange != null) onPreferedSizeChange.run();
        }
    }
}
