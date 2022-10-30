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

package kr.syeyoung.dungeonsguide.mod.gui.elements;

import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class MScrollBar extends MPanel {
    private final Axis axis;

    @Getter
    private int thumbSize, max, min;
    @Setter
    @Getter
    private int current;

    @Getter
    @Setter
    private int width = 10;

    public void setMax(int max) {
        if (max < min) max = min;
        this.max = max;

        current = MathHelper.clamp_int(current, min, max);
        if (onUpdate != null) onUpdate.run();
    }

    public void setMin(int min) {
        if (max < min) max = min;
        this.min = min;

        current = MathHelper.clamp_int(current, min, max);
        if (onUpdate != null) onUpdate.run();
    }

    public void setThumbSize(int thumbSize) {
        this.thumbSize = thumbSize;

        current = MathHelper.clamp_int(current, min, max);
        if (onUpdate != null) onUpdate.run();
    }

    public void addToCurrent(int dv) {
        int current2 = current + dv;

        current = MathHelper.clamp_int(current2, min, max);

        if (onUpdate != null) onUpdate.run();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(axis == Axis.X ? -1 : width, axis == Axis.Y ? -1 : width);
    }

    @Getter
    @Setter
    private int background = RenderUtils.blendAlpha(0xFF141414, 0.04f),
            thumb = RenderUtils.blendAlpha(0xFF141414, 0.14f),
            thumbHover = RenderUtils.blendAlpha(0xFF141414, 0.15f),
            thumbClick = RenderUtils.blendAlpha(0xFF141414, 0.20f);

    private Runnable onUpdate;

    public MScrollBar(int min, int max, int thumbSize, int current, Axis axis, Runnable onUpdate) {
        if (max < min) max = min;
        this.min = min; this.min = max; this.thumbSize = thumbSize; this.current = current; this.axis = axis;
        this.current = MathHelper.clamp_int(current, min, max);
        this.onUpdate = onUpdate;
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        lastThumbRect.width = 0; lastThumbRect.height = 0;
    }

    private Rectangle lastThumbRect = new Rectangle();
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        // RENDER SUPER NICE SCROLL BAR
        int minimalThumbLen = 20;

        Gui.drawRect(0,0,getBounds().width, getBounds().height, background);
        int length = axis == Axis.X ? bounds.width :bounds.height;
        int totalUnscaledLength = max-min + thumbSize;
        double subPoint = ((double)max-min) * length / totalUnscaledLength;
        if (length - subPoint < minimalThumbLen) {
            subPoint = length - minimalThumbLen;
        }
        double thumbSize = length - subPoint;

        double startPt;
        if (max - min == 0) {
            startPt =0;
        } else {
            startPt = subPoint * (current - min)/((double)max - min);
        }

        int color = thumbHover;
        if (getBounds().contains(relMousex0, relMousey0)) color = thumbHover;
        if (grabbed) color = thumbClick;

        if (axis == Axis.X) {
            int startX = (int) startPt;
            int endX = (int) (startPt + thumbSize);

            Gui.drawRect(startX,0,endX,getBounds().height, color);
            lastThumbRect.x = startX; lastThumbRect.y = 0; lastThumbRect.width = endX - startX; lastThumbRect.height = getBounds().height;
        } else if (axis == Axis.Y) {
            int startY = (int) startPt;
            int endY = (int) (startPt + thumbSize);

            Gui.drawRect(0,startY,getBounds().width,endY, color);
            lastThumbRect.x = 0; lastThumbRect.y = startY; lastThumbRect.width = getBounds().width; lastThumbRect.height = endY - startY;
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

        int minimalThumbLen = 20;

        Gui.drawRect(0,0,getBounds().width, getBounds().height, background);
        int length = axis == Axis.X ? bounds.width :bounds.height;
        int totalUnscaledLength = max-min + thumbSize;
        double subPoint = ((double)max-min) * length / totalUnscaledLength;
        if (length - subPoint < minimalThumbLen) {
            subPoint = length - minimalThumbLen;
        }
        double thumbSize = length - subPoint;



        int dx = absMouseX - lastX, dy = absMouseY - lastY;

        lastX = absMouseX;
        lastY = absMouseY;

        int prevVal = current;
        if (axis == Axis.Y) {
            actualValue += dy * (max - min) / subPoint;
        } else if (axis == Axis.X) {
            actualValue += dx * (max - min) / subPoint;
        }

        current = MathHelper.clamp_int(actualValue, min, max);

        if (onUpdate != null && prevVal != current) onUpdate.run();
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {
        grabbed= false;
    }

    public static enum Axis {
        X, Y
    }


    @Override
    public void mouseMoved(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {
        if (grabbed)
            setCursor(EnumCursor.CLOSED_HAND);
        else if (lastThumbRect.contains(relMouseX0, relMouseY0)) {
            setCursor(EnumCursor.OPEN_HAND);
        }
    }
}
