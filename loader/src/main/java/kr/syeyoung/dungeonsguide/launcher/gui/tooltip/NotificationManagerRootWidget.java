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

package kr.syeyoung.dungeonsguide.launcher.gui.tooltip;

import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.launcher.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.launcher.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.launcher.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.launcher.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.launcher.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class NotificationManagerRootWidget extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "listApi")
    public final BindableAttribute<Column> api = new BindableAttribute<>(Column.class);

    public NotificationManagerRootWidget() {
        super(new ResourceLocation("dungeons_guide_loader:gui/tooltips/tooltipHolder.gui"));
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
}
