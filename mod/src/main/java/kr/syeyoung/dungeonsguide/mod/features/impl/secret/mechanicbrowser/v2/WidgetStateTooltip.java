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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.mechanicbrowser.v2;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WidgetStateTooltip extends AnnotatedWidget {

    @Bind(variableName = "children")
    public final BindableAttribute children = new BindableAttribute(WidgetList.class);
    @Bind(variableName = "scale")
    public final BindableAttribute<Double> scale = new BindableAttribute<>(Double.class);
    private DungeonMechanic mechanic;
    public WidgetStateTooltip(DungeonRoom dungeonRoom, DungeonMechanic mechanic, String mechanicId) {
        super(new ResourceLocation("dungeonsguide:gui/features/mechanicBrowser/tooltip.gui"));
        scale.setValue(FeatureRegistry.SECRET_BROWSE.getScale());
        this.mechanic = mechanic;

        Set<String> state = mechanic.getPossibleStates(dungeonRoom);
        List<Widget> widgetList = new ArrayList<>();
        for (String s : state) {
            widgetList.add(new WidgetState(dungeonRoom, mechanicId, s));
        }
        children.setValue(widgetList);
    }
}
