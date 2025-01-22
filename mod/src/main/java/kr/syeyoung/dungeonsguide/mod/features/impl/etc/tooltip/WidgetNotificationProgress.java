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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip;

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.FeatureRequestCalculation;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class WidgetNotificationProgress extends AnnotatedImportOnlyWidget implements Renderer {

    @Bind(variableName = "progresses")
    public final BindableAttribute<Column> progresses = new BindableAttribute<>(Column.class);
    @Bind(variableName = "progressTitle")
    public final BindableAttribute<String> progressTitle = new BindableAttribute<>(String.class);
    private UUID uuid;
    public WidgetNotificationProgress(UUID uuid, String title) {
        super(new ResourceLocation("dungeonsguide:gui/features/notifications/tooltipProgress.gui"));
        this.uuid = uuid;

        this.progressTitle.setValue(title);
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        if (progressUpdate) {
            progressUpdate = false;
            update(progressesData);
        }

        SingleChildRenderer.INSTANCE.doRender(partialTicks, context, buildContext);
    }

    @Override
    public void onMount() {
        progressUpdate = true;
    }

    @AllArgsConstructor
    @Getter @Setter
    public static class Progress {
        private volatile String message;
        private AtomicInteger current;
        private AtomicInteger total;
        private final boolean bar;
    }
    private List<Progress> progressesData = new CopyOnWriteArrayList<>();
    private volatile boolean progressUpdate = false;

    public void addProgress(Progress progress) {
        this.progressesData.add(progress);
        this.progressUpdate = true;
    }

    public void removeProgress(Progress progress) {
        this.progressesData.remove(progress);
        this.progressUpdate = true;
    }

    public void update(List<Progress> progresses) {
        if (this.progresses.getValue() != null) {
            Column column = this.progresses.getValue();
            column.removeAllWidget();;
            for (Progress progress : progresses) {
                column.addWidget(new WidgetProgressPart(progress));
            }
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


        private Progress progress;

        public WidgetProgressPart(Progress progress) {
            super(new ResourceLocation("dungeonsguide:gui/features/notifications/progresspart.gui"));
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
