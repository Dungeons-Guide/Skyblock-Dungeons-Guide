/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MTooltip;
import kr.syeyoung.dungeonsguide.launcher.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.elements.popups.AbsLocationPopup;
import kr.syeyoung.dungeonsguide.launcher.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.launcher.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.launcher.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.launcher.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.launcher.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.launcher.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompatLayer extends Widget implements Layouter, Renderer {
    private MPanel panel;
    private Rectangle force;

    public CompatLayer(MPanel panel) {
        this(panel, null);
    }

    List<AbsLocationPopup> tooltips = new ArrayList<>();
    public CompatLayer(MPanel panel, Rectangle force) {
        this.panel = panel;
        this.force = force;
        panel.setParent(new MPanel() {
            @Override
            public void setCursor(EnumCursor enumCursor) {
                getDomElement().setCursor(enumCursor);
            }

            @Override
            public int getTooltipsOpen() {
                return 0;
            }

            @Override
            public void openTooltip(MTooltip mPanel) {
                Rectangle bounds = mPanel.getBounds();
                AbsLocationPopup absLocationPopup = new AbsLocationPopup(bounds.getX(), bounds.getY(), new CompatLayer(mPanel, bounds), true);
                PopupMgr.getPopupMgr(getDomElement()).openPopup(absLocationPopup, (a) -> {tooltips.remove(absLocationPopup);});
                tooltips.add(absLocationPopup);
            }
        });
    }

    @Override
    public void onUnmount() {
        super.onUnmount();
        tooltips.forEach(PopupMgr.getPopupMgr(getDomElement())::closePopup);
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }



    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton) {
        getDomElement().obtainFocus();
        double scale = getDomElement().getAbsBounds().getWidth() / getDomElement().getSize().getWidth();
        return panel.mouseClicked0( (int)(absMouseX / scale), (int)(absMouseY / scale), (int)relMouseX,(int) relMouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        double scale = getDomElement().getAbsBounds().getWidth() / getDomElement().getSize().getWidth();
        panel.mouseClickMove0( (int)(absMouseX / scale), (int)(absMouseY / scale),  (int)relMouseX, (int)relMouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        double scale = getDomElement().getAbsBounds().getWidth() / getDomElement().getSize().getWidth();
        panel.mouseReleased0( (int)(absMouseX / scale), (int)(absMouseY / scale), (int)relMouseX,(int) relMouseY, state);
    }

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
        double scale = getDomElement().getAbsBounds().getWidth() / getDomElement().getSize().getWidth();
        panel.mouseMoved0( (int)(absMouseX / scale), (int)(absMouseY / scale), (int)relMouseX0, (int)relMouseY0);
        return true;
    }

    @Override
    public boolean mouseScrolled(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int scrollAmount) {
        double scale = getDomElement().getAbsBounds().getWidth() / getDomElement().getSize().getWidth();
        panel.mouseScrolled0( (int)(absMouseX / scale), (int)(absMouseY / scale), (int)relMouseX0, (int)relMouseY0, scrollAmount);
        return true;
    }

    @Override
    public void keyPressed(char typedChar, int keyCode) {
        panel.keyPressed0(typedChar, keyCode);
    }

    @Override
    public void keyReleased(char typedChar, int keyCode) {
        panel.keyReleased0(typedChar, keyCode);
    }

    @Override
    public void keyHeld(char typedChar, int keyCode) {
        panel.keyHeld0(typedChar, keyCode);
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        Dimension dimension = force == null ? panel.getPreferredSize() : force.getSize();
        panel.resize((int) constraintBox.getMaxWidth(), (int) constraintBox.getMaxHeight());
        if (panel.getBounds().getWidth() != 0) dimension = panel.getSize();

        panel.setBounds(new Rectangle(0,0, (int) dimension.getWidth(), (int) dimension.getHeight()));
        return new Size(dimension.getWidth(), dimension.getHeight());
    }

    @Override
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        double scale = getDomElement().getAbsBounds().getWidth() / getDomElement().getSize().getWidth();

        Rectangle originalRect = context.currentClip();
        Rectangle rectangle = originalRect == null ? null : originalRect.getBounds();
        boolean isNotNull = rectangle != null;
        if (rectangle == null) rectangle = new Rectangle(0,0,Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

        rectangle.y = Minecraft.getMinecraft().displayHeight - rectangle.y - rectangle.height;
        rectangle.width /= scale;
        rectangle.height /= scale;
        rectangle.x /= scale;
        rectangle.y /= scale;

//        System.out.println(rectangle);
        panel.render0(scale, new Point((int) (getDomElement().getAbsBounds().getX() / scale), (int) (getDomElement().getAbsBounds().getY() /scale)),
                rectangle, (int)(absMouseX / scale), (int)(absMouseY / scale), (int)relMouseX, (int)relMouseY, partialTicks);

        if (isNotNull) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(originalRect.x, originalRect.y, originalRect.width, originalRect.height);
        }
    }
}
