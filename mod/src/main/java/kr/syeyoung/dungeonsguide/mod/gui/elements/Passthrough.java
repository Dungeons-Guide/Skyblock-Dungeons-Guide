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

package kr.syeyoung.dungeonsguide.mod.gui.elements;

import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.modapi.ModAPI;

import java.util.Collections;
import java.util.List;

public class Passthrough extends AnnotatedExportOnlyWidget implements Layouter, Renderer{

    public Passthrough() {
    }


    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        Rect rect = buildContext.getAbsBounds();
        double w = buildContext.getSize().getWidth();
        double h = buildContext.getSize().getHeight();


        int screenHeight = ModAPI.getAPI().getDisplayHeight();
        int screenWidth = ModAPI.getAPI().getDisplayWidth();

        double sx = rect.getX() / screenWidth;
        double sy = (ModAPI.getAPI().getDisplayHeight() - rect.getY()) / screenHeight;
        double ex = (rect.getX() + rect.getWidth())/ screenWidth;
        double ey = (ModAPI.getAPI().getDisplayHeight() - rect.getY() - rect.getHeight()) / screenHeight;

        context.drawPassthrough(0,0, sx, sy, ex-sy, ey-sy, w, h);
    }
}
