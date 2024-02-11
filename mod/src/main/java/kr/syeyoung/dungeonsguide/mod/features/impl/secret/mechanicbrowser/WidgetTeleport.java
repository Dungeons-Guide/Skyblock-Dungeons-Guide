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

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

public class WidgetTeleport extends AnnotatedWidget {

    @Bind(variableName = "state")
    public final BindableAttribute<String> state = new BindableAttribute<>(String.class);

    private DungeonRoom dungeonRoom;
    private String  mechanic;

    public WidgetTeleport(DungeonRoom dungeonRoom, String mechanic) {
        super(new ResourceLocation("dungeonsguide:gui/features/mechanicBrowser/state.gui"));
        state.setValue("§eTeleport To");
        this.dungeonRoom = dungeonRoom;
        this.mechanic = mechanic;
    }

    @On(functionName = "navigate")
    public void navigate() {
        BlockPos pos = dungeonRoom.getMechanics().get(mechanic).getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom);
        Minecraft.getMinecraft().thePlayer.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }
}
