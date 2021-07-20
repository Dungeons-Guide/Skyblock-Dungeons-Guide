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
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.MathHelper;
import org.w3c.dom.css.Rect;

import java.awt.*;

public class MScrollablePanel extends MPanel {
    @Getter
    private boolean hideScrollBarWhenNotNecessary = false;

    public void setHideScrollBarWhenNotNecessary(boolean hideScrollBarWhenNotNecessary) {
        this.hideScrollBarWhenNotNecessary = hideScrollBarWhenNotNecessary;
        setBounds(getBounds());
    }

    private final int axis; // 1: Y 2: X 3: both.


    private Dimension totalContentArea = new Dimension();

    private MPanel viewPort;
    @Getter
    private MPanel contentArea;
    @Getter
    private MScrollBar scrollBarX, scrollBarY;

    private Rectangle contentAreaDim;

    public MScrollablePanel(int axis) {
        this.axis = axis;
        viewPort = new MPanel();
        scrollBarX = new MScrollBar(0, 1, 1, 0, MScrollBar.Axis.X, this::onScrollBarUpdate);
        scrollBarY = new MScrollBar(0, 1, 1, 0, MScrollBar.Axis.Y, this::onScrollBarUpdate);

        if ((axis & 1) > 0)
            super.add(scrollBarY);
        if ((axis & 2) > 0)
            super.add(scrollBarX);
        super.add(viewPort);


        contentArea = new MPanel() {
            @Override
            public void add(MPanel child) {
                super.add(child);
                evalulateContentArea();
            }

            @Override
            public void remove(MPanel panel) {
                super.remove(panel);
                evalulateContentArea();
            }

            @Override
            public void setBounds(Rectangle bounds) {
                if (bounds == null) return;
                this.bounds.x = bounds.x;
                this.bounds.y = bounds.y;
                this.bounds.width = bounds.width;
                this.bounds.height = bounds.height;

                onBoundsUpdate();
            }

            @Override
            public void resize(int parentWidth, int parentHeight) {
                for (MPanel childComponent : childComponents) {
                    childComponent.resize0(parentWidth, parentHeight);
                }
                evalulateContentArea();
            }
        };
        viewPort.add(contentArea);
        contentArea.setIgnoreBoundOnClip(true);
        evalulateContentArea();
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        super.render(absMousex, absMousey, relMousex0, relMousey0, partialTicks, scissor);

        boolean hideX = false, hideY = false;
        if (bounds.width > contentAreaDim.width && hideScrollBarWhenNotNecessary) hideX = true;
        if (bounds.height > contentAreaDim.height && hideScrollBarWhenNotNecessary) hideY = true;
        if (axis == 3 && !(hideX && hideY)) {
            Gui.drawRect(scrollBarX.getBounds().width, scrollBarY.getBounds().height, getBounds().width, getBounds().height, RenderUtils.blendAlpha(0x141414, 0.03f));
        }
    }

    private void evalulateContentArea() {
        if (contentArea.getChildComponents().size() == 0) {
            contentAreaDim= new Rectangle(0,0,0,0);
            return;
        }
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (MPanel childComponent : contentArea.getChildComponents()) {
            Rectangle bounds = childComponent.getBounds();
            if (bounds.x < minX) minX = bounds.x;
            if (bounds.x + bounds.width > maxX) maxX = bounds.x + bounds.width;
            if (bounds.y < minY) minY = bounds.y;
            if (bounds.y + bounds.height > maxY) maxY = bounds.y + bounds.height;
        }
        contentAreaDim = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        scrollBarX.setMin(contentAreaDim.x);
        scrollBarX.setMax(contentAreaDim.x + contentAreaDim.width);
        scrollBarY.setMin(contentAreaDim.y);
        scrollBarY.setMax(contentAreaDim.y + contentAreaDim.height);
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        boolean hideX = false, hideY = false;
        if (bounds.width > contentAreaDim.width && hideScrollBarWhenNotNecessary) hideX = true;
        if (bounds.height > contentAreaDim.height && hideScrollBarWhenNotNecessary) hideY = true;

        if (axis == 3 && !(hideX || hideY)) {
            Dimension preferedX = scrollBarX.getPreferredSize();
            Dimension preferedY = scrollBarY.getPreferredSize();
            scrollBarY.setBounds(new Rectangle(bounds.width - preferedY.width, 0, preferedY.width, bounds.height - preferedX.height));
            scrollBarX.setBounds(new Rectangle(0, bounds.height - preferedX.height, bounds.width - preferedY.width, preferedX.height));
        } else if (axis == 2 || (axis == 3 && hideY)) {
            Dimension preferedX = scrollBarX.getPreferredSize();
            scrollBarY.setBounds(new Rectangle(0,0,0,0));
            scrollBarX.setBounds(new Rectangle(0, bounds.height - preferedX.height, bounds.width, preferedX.height));
        } else if (axis == 1 || (axis == 3 && hideX)) {
            Dimension preferedY = scrollBarY.getPreferredSize();
            scrollBarX.setBounds(new Rectangle(0,0,0,0));
            scrollBarY.setBounds(new Rectangle(bounds.width - preferedY.width, 0, preferedY.width, bounds.height));
        }

        viewPort.setBounds(new Rectangle(0,0,bounds.width-scrollBarY.getBounds().width, bounds.height - scrollBarX.getBounds().height));

        scrollBarX.setThumbSize(viewPort.getBounds().width);
        scrollBarY.setThumbSize(viewPort.getBounds().height);
    }


    private void onScrollBarUpdate() {
        contentArea.setPosition(new Point(-scrollBarX.getCurrent(), -scrollBarY.getCurrent()));
    }


    @Override
    public void add(MPanel child) { contentArea.add(child); }

    @Override
    public void remove(MPanel panel) { contentArea.remove(panel); }

    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (lastAbsClip.contains(absMouseX, absMouseY) && (axis == 1 || axis == 3)) {
            scrollBarY.addToCurrent(MathHelper.clamp_int(scrollAmount, -1, 1) * -30);
        }
    }
}
