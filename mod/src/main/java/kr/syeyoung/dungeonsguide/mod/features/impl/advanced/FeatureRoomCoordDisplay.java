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

package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;


import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureRoomCoordDisplay extends TextHUDFeature {
    public FeatureRoomCoordDisplay() {
        super("Debug", "Display Coordinate Relative to the Dungeon Room and room's rotation", "X: 0 Y: 3 Z: 5 Facing: Z+" , "advanced.coords");
        this.setEnabled(false);
        getStyles().add(new TextStyle("coord", new AColor(Color.yellow.getRGB(),true), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("X: 0 Y: 3 Z: 5 Facing: Z+","coord"));
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    private static final String[] facing = {"Z+", "X-", "Z-", "X+"};

    @Override
    public boolean isHUDViewable() {
        if (!skyblockStatus.isOnDungeon()) return false;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return false;

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (context.getScaffoldParser() == null) return false;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) {
            return false;
        }
        return true;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Collections.singletonList("coord");
    }

    @Override
    public List<StyledText> getText() {
        if (!skyblockStatus.isOnDungeon()) return Collections.emptyList();
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return Collections.emptyList();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) {
            return Collections.emptyList();
        }

        int facing = (int) (thePlayer.rotationYaw + 45) % 360;
        if (facing < 0) facing += 360;
        int real = (facing / 90 + dungeonRoom.getRoomMatcher().getRotation()) % 4;

        OffsetPoint offsetPoint = new OffsetPoint(dungeonRoom, new BlockPos((int)thePlayer.posX, (int)thePlayer.posY, (int)thePlayer.posZ));

        return Collections.singletonList(new StyledText("X: "+offsetPoint.getX()+" Y: "+offsetPoint.getY()+" Z: "+offsetPoint.getZ()+" Facing: "+ FeatureRoomCoordDisplay.facing[real], "coord"));
    }

}
