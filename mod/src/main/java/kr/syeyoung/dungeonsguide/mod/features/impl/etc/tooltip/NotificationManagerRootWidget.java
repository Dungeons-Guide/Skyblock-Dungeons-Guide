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

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationManagerRootWidget extends AnnotatedImportOnlyWidget implements Renderer {
    @Bind(variableName = "listApi")
    public final BindableAttribute<Column> api = new BindableAttribute<>(Column.class);

    public NotificationManagerRootWidget() {
        super(new ResourceLocation("dungeonsguide:gui/features/notifications/tooltipHolder.gui"));
    }

    private final Map<UUID, Widget> tooltipList = new HashMap<>();

    public void updateNotification(UUID uid, Widget tooltip) {
        Widget old = tooltipList.put(uid, tooltip);
        if (old != null) api.getValue().removeWidget(old);
        api.getValue().addWidget(tooltip);
    }
    public void removeNotification(UUID uid) {
        Widget old = tooltipList.remove(uid);
        if (old != null) api.getValue().removeWidget(old);
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {


        SingleChildRenderer.INSTANCE.doRender(partialTicks, context, buildContext);
    }
}
