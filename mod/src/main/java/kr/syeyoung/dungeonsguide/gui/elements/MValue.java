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

import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditOffsetPointSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.List;
public class MValue<T> extends MPanel {
    @Getter
    private final T data;
    private ValueEditOffsetPointSet valueEditOffsetPointSet;
    private final MLabel dataLab;

    @Getter @Setter
    private Color hover = Color.gray;

    private final List<MPanel> addons;

    public MValue(final T parameter, List<MPanel> addons) {
        this.addons = addons;
        this.add(this.dataLab = new MLabel() {
            @Override
            public String getText() {
                return data != null ?data.toString() :"-empty-";
            }
        });
        this.dataLab.setAlignment(MLabel.Alignment.RIGHT);

        this.data = parameter;
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (hover != null && new Rectangle(new Point(0,0),getBounds().getSize()).contains(relMousex0, relMousey0)) {
            Gui.drawRect(0,0,getBounds().width, getBounds().height, hover.getRGB());
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (this.getBounds().x > -20 && lastAbsClip.contains(absMouseX, absMouseY)) {
            EditingContext.getEditingContext().openGui(new GuiDungeonValueEdit(data, addons));
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setSize(new Dimension(parentWidth, getBounds().height));
        dataLab.setBounds(new Rectangle(0,0,parentWidth, getBounds().height));
    }

    @Override
    public void onBoundsUpdate() {
        dataLab.setBounds(new Rectangle(0,0,getBounds().width, getBounds().height));
    }
}
