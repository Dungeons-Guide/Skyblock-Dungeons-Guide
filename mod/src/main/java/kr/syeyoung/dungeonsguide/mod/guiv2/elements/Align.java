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
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

public class Align extends AnnotatedExportOnlyWidget implements Layouter {
    @Export(attributeName = "hAlign")
    public final BindableAttribute<Alignment> hAlign = new BindableAttribute<>(Alignment.class, Alignment.CENTER);
    @Export(attributeName = "vAlign")
    public final BindableAttribute<Alignment> vAlign = new BindableAttribute<>(Alignment.class, Alignment.CENTER);

    @Export(attributeName = "$")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);
    public static enum Alignment {
        START, CENTER, END
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(child.getValue());
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        DomElement theOnly = getDomElement().getChildren().get(0);
        Size size = theOnly.getLayouter().layout(theOnly, new ConstraintBox(
                0, constraintBox.getMaxWidth(), 0, constraintBox.getMaxHeight()
        ));
        theOnly.setRelativeBound(new Rect(
                (constraintBox.getMaxWidth() - size.getWidth())/2,
                (constraintBox.getMaxHeight() - size.getHeight())/2,
                size.getWidth(), size.getHeight()
        ));
        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }
}
