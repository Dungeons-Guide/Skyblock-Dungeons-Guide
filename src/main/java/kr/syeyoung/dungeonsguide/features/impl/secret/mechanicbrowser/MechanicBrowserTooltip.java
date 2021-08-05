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

package kr.syeyoung.dungeonsguide.features.impl.secret.mechanicbrowser;

import kr.syeyoung.dungeonsguide.gui.elements.MList;
import kr.syeyoung.dungeonsguide.gui.elements.MTooltip;
import lombok.Getter;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class MechanicBrowserTooltip extends MTooltip {
    @Getter
    private MList mList;
    public MechanicBrowserTooltip() {
        mList = new MList();
        mList.setGap(0);
        add(mList);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        Dimension effectiveDim = getEffectiveDimension();
        Gui.drawRect(0, 0, effectiveDim.width, effectiveDim.height, 0xFF444444);
        Gui.drawRect(1, 1, effectiveDim.width - 1, effectiveDim.height - 1, 0xFF262626);
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        mList.setBounds(new Rectangle(1,1, getEffectiveDimension().width-2, getEffectiveDimension().height-2));
        mList.realignChildren();
    }

    @Override
    public void setScale(double scale) {
        super.setScale(scale);
        mList.setBounds(new Rectangle(1,1, getEffectiveDimension().width-2, getEffectiveDimension().height-2));
        mList.realignChildren();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dim = mList.getPreferredSize();
        return new Dimension((int) ((dim.width + 2) * getScale()), (int) ((dim.height + 2) * getScale()));
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (!lastAbsClip.contains(absMouseX, absMouseY)) close();
    }
}
