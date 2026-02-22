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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.waypoints;

import kr.syeyoung.dungeonsguide.mod.features.impl.boss.FeatureF7TerminalWaypoints;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Placeholder;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class WidgetTerminalWaypointsEditor extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "config")
    public final BindableAttribute<Widget> config = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "preview")
    public final BindableAttribute<Widget> preview = new BindableAttribute<>(Widget.class);

    public WidgetTerminalWaypointsEditor(FeatureF7TerminalWaypoints lineProperties) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/f7waypoints/editor.gui"));
        this.config.setValue(new WidgetTerminalWaypointEdit(lineProperties));
//        this.preview.setValue(new WidgetPreview(lineProperties)); $$ PREVIEW
        this.preview.setValue(new Placeholder());
    }
}
