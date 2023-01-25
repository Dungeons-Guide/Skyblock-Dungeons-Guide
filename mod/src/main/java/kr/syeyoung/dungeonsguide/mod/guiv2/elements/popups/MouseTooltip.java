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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.util.Collections;
import java.util.List;

public class MouseTooltip extends Widget {
    private final AbsLocationPopup absLocationPopup;

    private final BindableAttribute<Double> x = new BindableAttribute<>(Double.class, (double)Mouse.getX());
    private final BindableAttribute<Double> y = new BindableAttribute<>(Double.class, (double)Mouse.getY());

    public MouseTooltip(Widget content) {
        absLocationPopup = new AbsLocationPopup(x,y, content, false);
    }
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(absLocationPopup);
    }

    @Override
    public void onUnmount() {
        super.onUnmount();
        x.unexportAll();
        y.unexportAll();
    }

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
        x.setValue((double) absMouseX);
        y.setValue((double) absMouseY);
        return false;
    }
}
