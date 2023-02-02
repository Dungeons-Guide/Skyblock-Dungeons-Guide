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

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.util.LinkedList;
import java.util.List;

public class Border extends AnnotatedExportOnlyWidget implements Layouter {
    @Export(attributeName = "_left")
    public final BindableAttribute<Widget> left = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "_right")
    public final BindableAttribute<Widget> right = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "_top")
    public final BindableAttribute<Widget> top = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "_bottom")
    public final BindableAttribute<Widget> bottom = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "_content")
    public final BindableAttribute<Widget> content = new BindableAttribute<>(Widget.class);

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        // layout borders, ask them about their constraints,
        // then layout content with space less than blahblah
        // then relayout borders.
        DomElement top = null, bottom = null, left = null, right = null, content = null;
        for (DomElement child : buildContext.getChildren()) {
            if (child.getWidget() == this.top.getValue()) top = child;
            if (child.getWidget() == this.bottom.getValue()) bottom = child;
            if (child.getWidget() == this.left.getValue()) left = child;
            if (child.getWidget() == this.right.getValue()) right = child;
            if (child.getWidget() == this.content.getValue()) content = child;
        }



        double th = 0, bh = 0;
        {
            if (top != null)
                th= top.getLayouter().layout(top, new ConstraintBox(constraintBox.getMaxWidth(), constraintBox.getMaxWidth(), 0, constraintBox.getMaxHeight())).getHeight();
            if (bottom != null)
                bh = bottom.getLayouter().layout(bottom, new ConstraintBox(constraintBox.getMaxWidth(), constraintBox.getMaxWidth(), 0, constraintBox.getMaxHeight())).getHeight();
        }

        double lw = 0, rw = 0;
        {
            if (left != null)
                lw= left.getLayouter().layout(left, new ConstraintBox(0, constraintBox.getMaxWidth(), constraintBox.getMaxHeight(), constraintBox.getMaxHeight())).getWidth();
            if (right != null)
                rw= right.getLayouter().layout(right, new ConstraintBox(0, constraintBox.getMaxWidth(), constraintBox.getMaxHeight(), constraintBox.getMaxHeight())).getWidth();
        }

        Size dimension = content.getLayouter().layout(content, new ConstraintBox(
                0, constraintBox.getMaxWidth() - lw - rw,
                0, constraintBox.getMaxHeight() - th - bh
        ));


        {
            if (left != null)
                lw= left.getLayouter().layout(left, new ConstraintBox(lw, lw, dimension.getHeight(), dimension.getHeight())).getWidth();
            if (right != null)
                rw= right.getLayouter().layout(right, new ConstraintBox(rw, rw, dimension.getHeight(), dimension.getHeight())).getWidth();
        }
        {
            if (top != null)
                th= top.getLayouter().layout(top, new ConstraintBox(dimension.getWidth() + lw + rw, dimension.getWidth() + lw + rw,  th, th)).getHeight();
            if (bottom != null)
                bh = bottom.getLayouter().layout(buildContext, new ConstraintBox(dimension.getWidth() + lw + rw, dimension.getWidth() + lw + rw,  bh,bh)).getHeight();
        }

        if (top != null)
            top.setRelativeBound(new Rect(0,0, dimension.getWidth() + lw + rw, th));
        if (bottom != null)
            bottom.setRelativeBound(new Rect(0, dimension.getHeight() + th, dimension.getWidth() + lw + rw, bh));
        if (left != null)
            left.setRelativeBound(new Rect(0, th, lw, dimension.getHeight()));
        if (right != null)
            right.setRelativeBound(new Rect(lw + dimension.getWidth(), th, rw, dimension.getHeight()));

        content.setRelativeBound(new Rect(
                th, lw, dimension.getWidth(), dimension.getHeight()
        ));



        return new Size(dimension.getWidth() + lw + rw, dimension.getHeight() + th + bh);
    }



    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        DomElement top = null, bottom = null, left = null, right = null, content = null;
        for (DomElement child : buildContext.getChildren()) {
            if (child.getWidget() == this.top.getValue()) top = child;
            if (child.getWidget() == this.bottom.getValue()) bottom = child;
            if (child.getWidget() == this.left.getValue()) left = child;
            if (child.getWidget() == this.right.getValue()) right = child;
            if (child.getWidget() == this.content.getValue()) content = child;
        }
        double effHeight = height - top.getLayouter().getMaxIntrinsicHeight(top, 0)
                - bottom.getLayouter().getMaxIntrinsicHeight(bottom, 0);
        return content.getLayouter().getMaxIntrinsicWidth(content, effHeight)
                + left.getLayouter().getMaxIntrinsicWidth(left, effHeight)
                + right.getLayouter().getMaxIntrinsicWidth(right, effHeight);
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        DomElement top = null, bottom = null, left = null, right = null, content = null;
        for (DomElement child : buildContext.getChildren()) {
            if (child.getWidget() == this.top.getValue()) top = child;
            if (child.getWidget() == this.bottom.getValue()) bottom = child;
            if (child.getWidget() == this.left.getValue()) left = child;
            if (child.getWidget() == this.right.getValue()) right = child;
            if (child.getWidget() == this.content.getValue()) content = child;
        }
        double effWidth = width - left.getLayouter().getMaxIntrinsicHeight(top, 0)
                - right.getLayouter().getMaxIntrinsicHeight(bottom, 0);
        return content.getLayouter().getMaxIntrinsicHeight(content, effWidth)
                + top.getLayouter().getMaxIntrinsicHeight(top, width)
                + bottom.getLayouter().getMaxIntrinsicHeight(bottom, width);
    }

    @Override
    protected Renderer createRenderer() {
        return OnlyChildrenRenderer.INSTANCE;
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        List<Widget> widgets = new LinkedList<>();
        widgets.add(content.getValue());
        if (top.getValue() != null)
            widgets.add(top.getValue());
        if (bottom.getValue() != null)
            widgets.add(bottom.getValue());
        if (left.getValue() != null)
            widgets.add(left.getValue());
        if (right.getValue() != null)
            widgets.add(right.getValue());
        return widgets;
    }
}
