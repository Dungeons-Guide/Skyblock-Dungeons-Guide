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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MTooltip extends MPanel {
    @Getter @Setter
    private MRootPanel root;

    public void open(MPanel component) {
        component.openTooltip(this);
    }
    public void close() {
        if (root != null)
        root.removeTooltip(this);
    }

    @Override
    public int getTooltipsOpen() {
        return super.getTooltipsOpen() - 1;
    }

    public void render0(ScaledResolution resolution, Point parentPoint, Rectangle parentClip, int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) { // 0,0 - a a
        int relMousex = relMousex0 - getBounds().x;
        int relMousey = relMousey0 - getBounds().y;

        GlStateManager.translate(getBounds().x, getBounds().y, 300);
        GlStateManager.color(1,1,1,0);


        Rectangle clip = getBounds().getBounds();
        GlStateManager.pushAttrib();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        clip(resolution, clip.x, clip.y, clip.width, clip.height);

        GlStateManager.pushAttrib();
        GuiScreen.drawRect(0,0, getBounds().width, getBounds().height,  backgroundColor.getRGB());
        GlStateManager.enableBlend();
        GlStateManager.popAttrib();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        render(absMousex, absMousey, relMousex, relMousey, partialTicks, clip);
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popAttrib();


        Point newPt = new Point(parentPoint.x + getBounds().x, parentPoint.y + getBounds().y);

        for (MPanel mPanel : getChildComponents()){
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            mPanel.render0(resolution, newPt, clip, absMousex, absMousey, relMousex, relMousey, partialTicks);
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }
}
