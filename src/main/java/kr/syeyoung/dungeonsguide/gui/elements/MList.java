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
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class MList extends MPanel {
    @Getter
    private int gap = 5;

    @Getter @Setter
    private boolean drawLine = true;

    public void setGap(int gap) {
        this.gap = gap;
        realignChildren();
    }

    private final int gapLineColor = 0xFFFFFFFF;

    public void realignChildren() {
        int y = 0;
        for (MPanel childComponent : getChildComponents()) {
            Dimension preferedSize = childComponent.getPreferredSize();
            int height = Math.max(10, preferedSize.height);
            childComponent.setBounds(new Rectangle(0, y, bounds.width, height));
            y += height;
            if (gap > 0)
                y += gap;
        }
        setSize(new Dimension(getSize().width, Math.max(0, y-gap)));
    }

    @Override
    public Dimension getPreferredSize() {
        int maxW = 0;
        int h = 0;
        for (MPanel childComponent : getChildComponents()) {
            Dimension preferedSize = childComponent.getPreferredSize();
            int height = Math.max(10, preferedSize.height);
            if (preferedSize.width > maxW) maxW = preferedSize.width;
            h += height;
            if (gap > 0) h += gap;
        }
        return new Dimension(maxW, Math.max(0, h-gap));
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (gap <= 0) return;
        if (!drawLine) return;
        for (int i = 1; i < getChildComponents().size(); i++) {
            MPanel panel = getChildComponents().get(i);
            Rectangle bound = panel.getBounds();
            Gui.drawRect(0, bound.y - gap, getBounds().width, bound.y, gapLineColor);
        }
    }

    @Override
    public void add(MPanel child) {
        super.add(child);
        realignChildren();
    }

    @Override
    public void remove(MPanel panel) {
        super.remove(panel);
        realignChildren();
    }
}
