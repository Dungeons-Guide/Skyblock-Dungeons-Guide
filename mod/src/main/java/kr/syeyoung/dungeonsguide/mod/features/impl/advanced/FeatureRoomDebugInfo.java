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
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureRoomDebugInfo extends TextHUDFeature {
    public FeatureRoomDebugInfo() {
        super("Debug", "Display Room Debug Info", "ONLY WORKS WITH SECRET SETTING", "advanced.debug.roominfo");
        this.setEnabled(false);
        getStyles().add(new TextStyle("info", new AColor(Color.white.getRGB(),true), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Line 1\nLine 2\nLine 3\nLine 4\nLine 5","info"));
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Collections.singletonList("info");
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
    public List<StyledText> getText() {
        if (!skyblockStatus.isOnDungeon()) return Collections.emptyList();
        if (!FeatureRegistry.DEBUG.isEnabled()) return Collections.emptyList();
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return Collections.emptyList();
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (context.getScaffoldParser() == null) return Collections.emptyList();
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
        return Collections.singletonList(new StyledText(str, "info"));
    }
}
