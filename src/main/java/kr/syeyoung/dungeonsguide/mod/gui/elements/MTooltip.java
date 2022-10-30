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

package kr.syeyoung.dungeonsguide.mod.gui.elements;

import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MTooltip extends MPanelScaledGUI {
    @Getter @Setter
    private MRootPanel root;

    public void open(MPanel component) {
        if (root == null)
        component.openTooltip(this);
    }
    public void close() {
        if (root != null)
        root.removeTooltip(this);
    }


    public boolean isOpen() {
        return root != null;
    }

    @Override
    public int getTooltipsOpen() {
        return super.getTooltipsOpen() - 1;
    }

    @Override
    public void render0(double parentScale, Point parentPoint, Rectangle parentClip, int absMousex0, int absMousey0, int relMousex0, int relMousey0, float partialTicks) {
        lastParentPoint = parentPoint;

        GlStateManager.translate(getBounds().x, getBounds().y, 300);
        GlStateManager.color(1,1,1,1);

        Rectangle absBound = getBounds().getBounds();
        absBound.setLocation(absBound.x + parentPoint.x, absBound.y + parentPoint.y);

        Rectangle clip = absBound;
        lastAbsClip = clip;

        if (clip.getSize().height * clip.getSize().width == 0) return;

        int absMousex = (int) (absMousex0 / scale), absMousey = (int) (absMousey0 / scale);
        int relMousex = (int) ((relMousex0 - getBounds().x) / scale);
        int relMousey = (int) ((relMousey0 - getBounds().y) /scale);

        // FROM HERE, IT IS SCALED

        GlStateManager.scale(this.scale, this.scale, 1);
        clip = new Rectangle((int) (clip.x / scale), (int) (clip.y / scale), (int) (clip.width / scale), (int) (clip.height / scale));
        lastAbsClip = clip;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        this.relativeScale = parentScale * this.scale;
        clip(clip.x, clip.y, clip.width, clip.height);


        GuiScreen.drawRect(0,0, (int) (getBounds().width / scale), (int) (getBounds().height / scale),  backgroundColor.getRGB());
        GlStateManager.enableBlend();


        GlStateManager.pushMatrix();

        render(absMousex, absMousey, relMousex, relMousey, partialTicks, clip);

        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);



        Point newPt = new Point((int) ((parentPoint.x + getBounds().x) / scale), (int) ((parentPoint.y + getBounds().y) / scale));

        for (MPanel mPanel : getChildComponents()){
            GlStateManager.pushMatrix();

            mPanel.render0(relativeScale, newPt,clip,absMousex, absMousey, relMousex, relMousey, partialTicks);

            GlStateManager.popMatrix();
        }
    }
}
