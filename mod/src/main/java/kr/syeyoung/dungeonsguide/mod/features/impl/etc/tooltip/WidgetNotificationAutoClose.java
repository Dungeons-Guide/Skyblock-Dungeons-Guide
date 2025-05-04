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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip;

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class WidgetNotificationAutoClose extends AnnotatedImportOnlyWidget implements Renderer {
    private Notification notification;


    @Bind(variableName = "title")
    public final BindableAttribute<String> title = new BindableAttribute<>(String.class);
    @Bind(variableName = "description")
    public final BindableAttribute<String> description = new BindableAttribute<>(String.class);
    @Bind(variableName = "color")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "closeVisibility")
    public final BindableAttribute<String> closeVisibility = new BindableAttribute<>(String.class);
    @Bind(variableName = "size")
    public final BindableAttribute<Size> size = new BindableAttribute<>(Size.class);
    @Bind(variableName = "width")
    public final BindableAttribute<Double> width = new BindableAttribute<>(Double.class);

    private UUID uuid;
    private long delay;
    private long now = -1;
    public WidgetNotificationAutoClose(UUID uuid, Notification notification, long delay) {
        super(new ResourceLocation("dungeonsguide:gui/features/notifications/tooltipClosing.gui"));
        this.notification =notification;
        title.setValue(notification.getTitle());
        color.setValue(notification.getTitleColor());
        description.setValue(notification.getDescription());
        this.uuid = uuid;
        this.delay = delay;

    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        if (notification.getOnClick() != null) notification.getOnClick().run();
        return true;
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        if (now == -1) now = System.currentTimeMillis();
        width.setValue((System.currentTimeMillis() - now)* size.getValue().getWidth() /delay );
        if (System.currentTimeMillis() - now > delay)
            FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(uuid);
        SingleChildRenderer.INSTANCE.doRender(partialTicks, context, buildContext);
    }

}
