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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.mechanicbrowser;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WidgetCategory extends AnnotatedWidget {
    @Bind(variableName = "categoryName")
    public final BindableAttribute<String> categoryName = new BindableAttribute<>(String.class);
    @Bind(variableName = "visible")
    public final BindableAttribute<String> visible = new BindableAttribute<>(String.class, "open");
    @Bind(variableName = "children")
    public final BindableAttribute children = new BindableAttribute<>(WidgetList.class);
    public WidgetCategory(String s, DungeonRoom dungeonRoom, Map<String, DungeonMechanic> dungeonMechanics, Consumer<String> onSelect) {
        super(new ResourceLocation("dungeonsguide:gui/features/mechanicBrowser/category.gui"));
        categoryName.setValue(s);

        List<Widget> widgets = new ArrayList<>();
        for (Map.Entry<String, DungeonMechanic> dungeonMechanic : dungeonMechanics.entrySet()) {
            widgets.add(new WidgetSecret(dungeonMechanic.getKey(), dungeonRoom, dungeonMechanic.getValue(), onSelect));
        }
        children.setValue(widgets);
    }

    @On(functionName = "toggle")
    public void toggle() {
        if (visible.getValue().equals("open"))
            visible.setValue("collapsed");
        else
            visible.setValue("open");
    }
}
