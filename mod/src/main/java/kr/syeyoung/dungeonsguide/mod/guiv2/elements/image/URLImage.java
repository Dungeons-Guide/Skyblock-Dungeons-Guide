/*
 * Dungeons Guide - The most Integerelligent Hypixel Skyblock Dungeons Mod
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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements.image;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

public class URLImage extends AnnotatedExportOnlyWidget implements Renderer, Layouter {
    @Export(attributeName="url")
    public final BindableAttribute<String> url = new BindableAttribute<String >(String.class);

    private ImageTexture imageTexture;

    public URLImage() {
        url.addOnUpdate((prev, neu) -> {
            ImageTexture.loadImage(neu, (texture) -> {
                this.imageTexture = texture;
                if (getDomElement() != null)
                    getDomElement().requestRelayout();
            });
        });
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        if (imageTexture == null) return;
        imageTexture.drawFrame(0,0,buildContext.getSize().getWidth(), buildContext.getSize().getHeight());
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        if (imageTexture == null) return new Size(constraintBox.getMinWidth(), constraintBox.getMinHeight());

        double heightIfWidthFit = constraintBox.getMaxWidth() * imageTexture.getHeight() / imageTexture.getWidth();
        double widthIfHeightFit = constraintBox.getMaxHeight() * imageTexture.getWidth() / imageTexture.getHeight();

        if (heightIfWidthFit <= constraintBox.getMaxHeight()) {
            return new Size(constraintBox.getMaxWidth(), Layouter.clamp(heightIfWidthFit, constraintBox.getMinHeight(), constraintBox.getMaxHeight()));
        } else if (widthIfHeightFit <= constraintBox.getMaxWidth()){
            return new Size(Layouter.clamp(widthIfHeightFit, constraintBox.getMinWidth(), constraintBox.getMaxWidth()), constraintBox.getMaxHeight());
        } else {
            throw new IllegalStateException("How is this possible mathematically?");
        }
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return imageTexture == null ? 0 : height * imageTexture.getWidth() / imageTexture.getHeight();
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        return imageTexture == null ? 0 : width * imageTexture.getHeight() / imageTexture.getWidth();
    }
}
