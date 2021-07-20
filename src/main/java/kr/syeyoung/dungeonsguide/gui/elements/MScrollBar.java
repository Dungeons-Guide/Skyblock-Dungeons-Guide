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

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class MScrollBar extends MPanel {
    private final Axis axis;
    @Setter
    @Getter
    private int thumbSize, max, min;
    @Setter
    @Getter
    private int current;

    public void addToCurrent(int dv) {
        int current2 = current + dv;

        current = MathHelper.clamp_int(current2, min, max - thumbSize);
        if (max - min < thumbSize) current = min;

        if (onUpdate != null) onUpdate.run();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(axis == Axis.X ? -1 : 20, axis == Axis.Y ? -1 : 20);
    }

    @Getter
    @Setter
    private int background = RenderUtils.blendAlpha(0xFF141414, 0.04f),
            thumb = RenderUtils.blendAlpha(0xFF141414, 0.08f),
            thumbHover = RenderUtils.blendAlpha(0xFF141414, 0.09f),
            thumbClick = RenderUtils.blendAlpha(0xFF141414, 0.14f);

    private Runnable onUpdate;

    public MScrollBar(int min, int max, int thumbSize, int current, Axis axis, Runnable onUpdate) {
        this.min = min; this.min = max; this.thumbSize = thumbSize; this.current = current; this.axis = axis;
        this.current = MathHelper.clamp_int(current, min, max - thumbSize);
        if (max - min < thumbSize) this.current = min;
        this.onUpdate = onUpdate;
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        // RENDER SUPER NICE SCROLL BAR
        Gui.drawRect(0,0,getBounds().width, getBounds().height, background);
        double startPerc, endPerc;
        if (max - min == 0) {
            startPerc =0; endPerc = 1;
        } else {
            startPerc = (current - min)/((double)max - min);
            endPerc = (current+thumbSize - min)/((double)max - min);
        }

        int color = thumbHover;
        if (getBounds().contains(relMousex0, relMousey0)) color = thumbHover;
        if (grabbed) color = thumbClick;

        if (axis == Axis.X) {
            int startX = (int) (startPerc * getBounds().width);
            int endX = (int) (endPerc * getBounds().width);
            endX = Math.max(endX, startX + 20);

            Gui.drawRect(startX,0,endX,getBounds().height, color);
        } else if (axis == Axis.Y) {
            int startY = (int) (startPerc * getBounds().height);
            int endY = (int) (endPerc * getBounds().height);
            endY = Math.max(endY, startY + 20);

            Gui.drawRect(0,startY,getBounds().width,endY, color);
        }
    }

    private int lastX;
    private int lastY;
    private int actualValue;
    private boolean grabbed;
    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (!lastAbsClip.contains(absMouseX, absMouseY)) return;
        if (getTooltipsOpen() > 0) return;
        grabbed = true;
        actualValue = current;
        lastX = absMouseX;
        lastY = absMouseY;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!grabbed) return;
        int dx = absMouseX - lastX, dy = absMouseY - lastY;

        lastX = absMouseX;
        lastY = absMouseY;

        int prevVal = current;

        if (axis == Axis.Y) {
            actualValue += dy * (max - min) / getBounds().height;
        } else if (axis == Axis.X) {
            actualValue += dx * (max - min) / getBounds().width;
        }

        current = MathHelper.clamp_int(actualValue, min, max - thumbSize);
        if (max - min < thumbSize) current = min;

        if (onUpdate != null && prevVal != current) onUpdate.run();
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {
        grabbed= false;
    }

    public static enum Axis {
        X, Y
    }
}
