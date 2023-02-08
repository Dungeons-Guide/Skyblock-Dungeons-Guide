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

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class FeatureRoomDebugInfo extends TextHUDFeature {
    public FeatureRoomDebugInfo() {
        super("Debug", "Display Room Debug Info", "ONLY WORKS WITH SECRET SETTING", "advanced.debug.roominfo");
        this.setEnabled(false);
        registerDefaultStyle("info", DefaultingDelegatingTextStyle.ofDefault().setTextShader(new AColor(Color.white.getRGB(),true)).setBackgroundShader(new AColor(0, 0,0,0)));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    @Override
    public TextSpan getDummyText() {
        return new TextSpan(getStyle("info"), "Line 1\nLine 2\nLine 3\nLine 4\nLine 5");
    }

    @Override
    public boolean isHUDViewable() {
        if (!skyblockStatus.isOnDungeon()) return false;
        if (!FeatureRegistry.DEBUG.isEnabled()) return false;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return false;
        return true;
    }

    @Override
    public TextSpan getText() {
        if (!skyblockStatus.isOnDungeon()) return new TextSpan(new NullTextStyle(), "");
        if (!FeatureRegistry.DEBUG.isEnabled()) return new TextSpan(new NullTextStyle(), "");
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return new TextSpan(new NullTextStyle(), "");
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (context.getScaffoldParser() == null) return new TextSpan(new NullTextStyle(), "");
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        String str = "";

        if (dungeonRoom == null) {
            if (context.getBossfightProcessor() == null) {
                str += "Where are you?!";
            } else {
                str += "You're prob in bossfight\n";
                str += "processor: "+context.getBossfightProcessor()+"\n";
                str += "phase: "+context.getBossfightProcessor().getCurrentPhase()+"\n";
                str += "nextPhase: "+ StringUtils.join(context.getBossfightProcessor().getNextPhases(), ",")+"\n";
                str += "phases: "+ StringUtils.join(context.getBossfightProcessor().getPhases(), ",");
            }
        } else {
            str +="you're in the room... color/shape/rot " + dungeonRoom.getColor() + " / " + dungeonRoom.getShape() + " / "+dungeonRoom.getRoomMatcher().getRotation()+"\n";
            str +="room uuid: " + dungeonRoom.getDungeonRoomInfo().getUuid() + (dungeonRoom.getDungeonRoomInfo().isRegistered() ? "" : " (not registered)")+"\n";
            str +="room name: " + dungeonRoom.getDungeonRoomInfo().getName()+"\n";
            str +="room state / max secret: " + dungeonRoom.getCurrentState() + " / "+dungeonRoom.getTotalSecrets();
        }
        return new TextSpan(getStyle("info"), str);
    }
}
