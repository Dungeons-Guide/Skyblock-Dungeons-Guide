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
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;

import java.awt.*;

public class FeatureRoomCoordDisplay extends TextHUDFeature {
    public FeatureRoomCoordDisplay() {
        super("Debug", "Display Coordinate Relative to the Dungeon Room and room's rotation", "X: 0 Y: 3 Z: 5 Facing: Z+" , "advanced.coords");
        this.setEnabled(false);
        registerDefaultStyle("coord", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.DEFAULT))
                .setTextShader(new AColor(Color.yellow.getRGB(),true)));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();


    @Override
    public TextSpan getDummyText() {
        return new TextSpan(getStyle("coord"), "X: 0 Y: 3 Z: 5 Facing: Z+");
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
    public TextSpan getText() {
        if (!skyblockStatus.isOnDungeon()) return new TextSpan(new NullTextStyle(), "");
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return new TextSpan(new NullTextStyle(), "");

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) {
            return new TextSpan(new NullTextStyle(), "");
        }

        int facing = (int) (thePlayer.rotationYaw + 45) % 360;
        if (facing < 0) facing += 360;
        int real = (facing / 90 + dungeonRoom.getRoomMatcher().getRotation()) % 4;

        OffsetPoint offsetPoint = new OffsetPoint(dungeonRoom, new BlockPos((int)thePlayer.posX, (int)thePlayer.posY, (int)thePlayer.posZ));

        return new TextSpan(getStyle("coord"), "X: "+offsetPoint.getX()+" Y: "+offsetPoint.getY()+" Z: "+offsetPoint.getZ()+" Facing: "+ FeatureRoomCoordDisplay.facing[real]);
    }

}
