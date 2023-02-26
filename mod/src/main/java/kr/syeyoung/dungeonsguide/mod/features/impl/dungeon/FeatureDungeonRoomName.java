/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.awt.*;

public class FeatureDungeonRoomName extends TextHUDFeature {
    public FeatureDungeonRoomName() {
        super("Dungeon.HUDs", "Display name of the room you are in", "Display name of the room you are in", "dungeon.roomname");
        registerDefaultStyle("in", DefaultingDelegatingTextStyle.derive("Feature Default - In", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("roomname", DefaultingDelegatingTextStyle.derive("Feature Default - Roomname", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    public int getTotalSecretsInt() {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        int totalSecrets = 0;
        for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
        }
        return totalSecrets;
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser() != null;
    }


    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("in"), "You're in "));
        dummyText.addChild(new TextSpan(getStyle("roomname"), "puzzle-tictactoe"));
        return dummyText;
    }

    @Override
    public TextSpan getText() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        Point roomPt = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(player.getPosition());
        DungeonRoom dungeonRoom = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser().getRoomMap().get(roomPt);
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("in"), "You're in "));
        if (dungeonRoom == null || dungeonRoom.getDungeonRoomInfo() == null) {
            actualBit.addChild(new TextSpan(getStyle("roomname"), "Unknown"));
        } else {
            actualBit.addChild(new TextSpan(getStyle("roomname"), dungeonRoom.getDungeonRoomInfo().getName()));
        }


        return actualBit;
    }

}
