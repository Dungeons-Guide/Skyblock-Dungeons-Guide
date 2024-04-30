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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.PathfindLineProperties;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

public class WidgetLinePropertiesEditor extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "config")
    public final BindableAttribute<Widget> config = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "preview")
    public final BindableAttribute<Widget> preview = new BindableAttribute<>(Widget.class);

    private PathfindLineProperties lineProperties;
    public WidgetLinePropertiesEditor(PathfindLineProperties lineProperties) {
        super(new ResourceLocation("dungeonsguide:gui/features/lineProperties/editor.gui"));
        this.config.setValue(new WidgetLineParamEdit(lineProperties));
        this.preview.setValue(new WidgetPreview(lineProperties));
        this.lineProperties = lineProperties;
    }
}
