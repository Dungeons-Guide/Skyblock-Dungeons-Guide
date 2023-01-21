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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

public class MouseTooltip extends AnnotatedWidget {
    @Bind(variableName = "x")
    public final BindableAttribute<Double> x = new BindableAttribute<>(Double.class);
    @Bind(variableName = "y")
    public final BindableAttribute<Double> y = new BindableAttribute<>(Double.class);;
    @Bind(variableName = "ref")
    public final BindableAttribute<DomElement> ref = new BindableAttribute<>(DomElement.class);
    @Bind(variableName = "child")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);
    public MouseTooltip(double mouseX, double mouseY, Widget child) {
        super(new ResourceLocation("dungeonsguide:gui/elements/locationedPopup.gui"));
        this.x.setValue(mouseX);
        this.y.setValue(mouseY);
        this.child.setValue(child);
    }

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
        x.setValue((double) absMouseX);
        y.setValue((double) absMouseY);
        return false;
    }
}
