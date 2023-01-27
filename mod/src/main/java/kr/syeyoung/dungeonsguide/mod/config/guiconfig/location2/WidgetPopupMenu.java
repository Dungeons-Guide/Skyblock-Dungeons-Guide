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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class WidgetPopupMenu extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "items")
    public final BindableAttribute widgetListBindableAttribute = new BindableAttribute<>(WidgetList.class);

    public WidgetPopupMenu(List<Widget> stuff) {
        super(new ResourceLocation("dungeonsguide:gui/config/popupmenu.gui"));
        widgetListBindableAttribute.setValue(stuff);
    }

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
        return true;
    }
}
