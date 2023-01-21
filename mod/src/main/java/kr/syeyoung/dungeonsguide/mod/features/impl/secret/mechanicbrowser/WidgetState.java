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

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.util.ResourceLocation;

public class WidgetState extends AnnotatedWidget {

    @Bind(variableName = "state")
    public final BindableAttribute<String> state = new BindableAttribute<>(String.class);

    private DungeonRoom dungeonRoom;
    private String  mechanic;
    private String s;

    public WidgetState(DungeonRoom dungeonRoom, String mechanic, String s) {
        super(new ResourceLocation("dungeonsguide:gui/features/mechanicBrowser/state.gui"));
        state.setValue(s);
        this.dungeonRoom = dungeonRoom;
        this.mechanic = mechanic;
        this.s = s;
    }

    @On(functionName = "navigate")
    public void navigate() {
        if (dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)
            ((GeneralRoomProcessor)dungeonRoom.getRoomProcessor()).pathfind("MECH-BROWSER", mechanic, s, FeatureRegistry.SECRET_LINE_PROPERTIES_SECRET_BROWSER.getRouteProperties());
    }
}
