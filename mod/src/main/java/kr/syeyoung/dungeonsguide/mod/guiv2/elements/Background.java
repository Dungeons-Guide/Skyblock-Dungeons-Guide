/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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


import kr.syeyoung.dungeonsguide.mod.guiv2.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

// passes down constraints
// but sets background!! cool!!!
public class Background extends AnnotatedExportOnlyWidget {

    @Export(attributeName = "backgroundColor")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class, 0xFFFFFFFF);

    @Export(attributeName = "_")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);

    @Override
    public List<Widget> build(DomElement buildContext) {
        return child.getValue() == null ? Collections.EMPTY_LIST : Collections.singletonList(child.getValue());
    }

    @Override
    protected Renderer createRenderer() {
        return new BRender();
    }

    public class BRender extends SingleChildRenderer {
        @Override
        public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext renderingContext, DomElement buildContext) {
            renderingContext.drawRect(0,0,buildContext.getSize().getWidth(),buildContext.getSize().getHeight(),
                    color.getValue()
            );
            super.doRender(absMouseX, absMouseY, relMouseX, relMouseY, partialTicks, renderingContext, buildContext);
        }
    }
}
