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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.triggers.mechanicbrowser;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.data.VectorI3D;

public class WidgetTeleport extends AnnotatedWidget {

    @Bind(variableName = "state")
    public final BindableAttribute<String> state = new BindableAttribute<>(String.class);

    private DungeonRoom dungeonRoom;
    private String  mechanic;

    public WidgetTeleport(DungeonRoom dungeonRoom, String mechanic) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/mechanicBrowser/state.gui"));
        state.setValue("§eTeleport To");
        this.dungeonRoom = dungeonRoom;
        this.mechanic = mechanic;
    }

    @On(functionName = "navigate")
    public void navigate() {
        VectorI3D pos = dungeonRoom.getMechanics().get(mechanic).getRepresentingPoint().getBlockPos(dungeonRoom);
        ModAPI.getAPI().getPlayer().sendMessageToServer("/tp "+pos.getX()+" "+pos.getY()+" " +pos.getZ());
    }
}
