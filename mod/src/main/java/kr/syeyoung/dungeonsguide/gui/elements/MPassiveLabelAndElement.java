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

public class MPassiveLabelAndElement extends MPanel {
    private final MLabel label;
    private final MPanel element;

    @Getter @Setter
    private Color hover;
    @Getter @Setter
    private Runnable onClick;

    @Getter @Setter
    private double divideRatio = 1/3.0;

    public MPassiveLabelAndElement(String label, MPanel element) {
        this.add(this.label = new MLabel());
        this.label.setText(label);
        this.add(element);
        this.element = element;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 20);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (hover != null && new Rectangle(new Point(0,0),getBounds().getSize()).contains(relMousex0, relMousey0)) {
            Gui.drawRect(0,0,getBounds().width, getBounds().height, hover.getRGB());
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (onClick!= null && lastAbsClip.contains(absMouseX, absMouseY)) {
            onClick.run();
        }
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0, (int) (getBounds().width * divideRatio), getBounds().height));
        element.setBounds(new Rectangle((int) (getBounds().width * divideRatio),0, (int) (getBounds().width * (1-divideRatio)), getBounds().height));
    }
}
