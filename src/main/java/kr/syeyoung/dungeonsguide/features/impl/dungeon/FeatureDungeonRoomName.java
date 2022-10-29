/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureDungeonRoomName extends TextHUDFeature {
    public FeatureDungeonRoomName() {
        super("Dungeon.HUDs", "Display name of the room you are in", "Display name of the room you are in", "dungeon.roomname", false, getFontRenderer().getStringWidth("You're in puzzle-tictactoe"), getFontRenderer().FONT_HEIGHT);
        getStyles().add(new TextStyle("in", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("roomname", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    public int getTotalSecretsInt() {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext();
        int totalSecrets = 0;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
        }
        return totalSecrets;
    }

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("You're in ","in"));
        dummyText.add(new StyledText("puzzle-tictactoe","roomname"));
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext().getMapProcessor() != null;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("roomname", "in");
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<StyledText> getText() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        Point roomPt = DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext().getMapProcessor().worldPointToRoomPoint(player.getPosition());
        DungeonRoom dungeonRoom = DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext().getRoomMapper().get(roomPt);
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("You're in ","in"));
        if (dungeonRoom == null) {
            actualBit.add(new StyledText("Unknown","roomname"));
        } else {
            actualBit.add(new StyledText(dungeonRoom.getDungeonRoomInfo().getName(),"roomname"));
        }


        return actualBit;
    }

}
