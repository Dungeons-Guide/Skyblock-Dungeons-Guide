/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;
import scala.collection.immutable.IntMap;

import java.util.List;

public class WidgetProgress extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "progresses")
    public final BindableAttribute<Column> progresses = new BindableAttribute<>(Column.class);
    @Bind(variableName = "progressVisible")
    public final BindableAttribute<String> visible = new BindableAttribute<>(String.class, "false");
    public WidgetProgress() {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/progress.gui"));
    }

    public void update(List<FeatureRequestCalculation.Progress> progresses) {
        if (this.progresses.getValue() != null) {
            Column column = this.progresses.getValue();
            column.removeAllWidget();;
            for (FeatureRequestCalculation.Progress progress : progresses) {
                column.addWidget(new WidgetProgressPart(progress));
            }
            visible.setValue(progresses.isEmpty() ? "false" : "true");
        }
    }

    public static class WidgetProgressPart extends AnnotatedImportOnlyWidget implements Renderer {
        @Bind(variableName = "size")
        public final BindableAttribute<Size> size = new BindableAttribute<>(Size.class);
        @Bind(variableName = "offset")
        public final BindableAttribute<Double> offset = new BindableAttribute<>(Double.class);
        @Bind(variableName = "width")
        public final BindableAttribute<Double> width = new BindableAttribute<>(Double.class);
        @Bind(variableName = "message")
        public final BindableAttribute<String> text = new BindableAttribute<>(String.class);
//        @Bind(variableName = "visible")
//        public final BindableAttribute<String> bar = new BindableAttribute<>(String.class);


        private FeatureRequestCalculation.Progress progress;

        public WidgetProgressPart(FeatureRequestCalculation.Progress progress) {
            super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/progresspart.gui"));
            this.progress = progress;
//            this.bar.setValue(progress.isBar()  ? "true" : "false");
            this.text.setValue(progress.getMessage());
            this.width.setValue(progress.isBar() ? 5.0 : 0.0);
            this.offset.setValue(0.0);
        }


        @Override
        public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
            text.setValue(progress.getMessage());
            if (progress.isBar())
                width.setValue(progress.getCurrent().get()* size.getValue().getWidth() /progress.getTotal().get());
            else {
                double way = (System.currentTimeMillis() % 3000) / 3000.0;
                double width = size.getValue().getWidth() / 5;
                double start = -width + (size.getValue().getWidth() + width) * way;
                double realStart = Math.max(0, start);
                double realEnd = Math.min(size.getValue().getWidth(), start + width);
                offset.setValue(realStart);
                this.width.setValue(realEnd - realStart);
            }
            SingleChildRenderer.INSTANCE.doRender(partialTicks, context, buildContext);
        }
    }
}
