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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureActions extends TextHUDFeature {
    public FeatureActions() {
        super("Dungeon.Secrets", "Action Viewer", "View List of actions that needs to be taken", "secret.actionview");

        getStyles().add(new TextStyle("pathfinding", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("mechanic", new AColor(0x55, 0xFF,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("state", new AColor(0x55, 0xFF,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("current", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("number",  new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("dot", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("action", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("afterline", new AColor(0xAA, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
    }


    @Override
    public boolean isHUDViewable() {
        if (!SkyblockStatus.isOnDungeon()) return false;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser() == null) return false;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return false;
        return dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor;
    }

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Pathfinding ","pathfinding"));
        dummyText.add(new StyledText("Secret ","mechanic"));
        dummyText.add(new StyledText("-> ","separator"));
        dummyText.add(new StyledText("Found\n","state"));
        dummyText.add(new StyledText("> ","current"));
        dummyText.add(new StyledText("1","number"));
        dummyText.add(new StyledText(". ","dot"));
        dummyText.add(new StyledText("Move ","action"));
        dummyText.add(new StyledText("OffsetPoint{x=1,y=42,z=1} \n","afterline"));
        dummyText.add(new StyledText("  ","current"));
        dummyText.add(new StyledText("2","number"));
        dummyText.add(new StyledText(". ","dot"));
        dummyText.add(new StyledText("Click ","action"));
        dummyText.add(new StyledText("OffsetPoint{x=1,y=42,z=1} \n","afterline"));
        dummyText.add(new StyledText("  ","current"));
        dummyText.add(new StyledText("3","number"));
        dummyText.add(new StyledText(". ","dot"));
        dummyText.add(new StyledText("Profit ","action"));
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("pathfinding","mechanic","separator","state","current", "number", "dot", "action", "afterline");
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }


    @Override
    public List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);

        for (ActionRoute path : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getPath().values()) {
            actualBit.add(new StyledText("Pathfinding ","pathfinding"));
            actualBit.add(new StyledText(path.getMechanic()+" ","mechanic"));
            actualBit.add(new StyledText("-> ","separator"));
            actualBit.add(new StyledText(path.getState()+"\n","state"));

            for (int i = Math.max(0,path.getCurrent()-2); i < path.getActions().size(); i++) {
                actualBit.add(new StyledText((i == path.getCurrent() ? ">" : " ") +" ","current"));
                actualBit.add(new StyledText(i+"","number"));
                actualBit.add(new StyledText(". ","dot"));
                AbstractAction action = path.getActions().get(i);
                String[] str = action.toString().split("\n");
                actualBit.add(new StyledText(str[0] + " ","action"));
                actualBit.add(new StyledText("(","afterline"));
                for (int i1 = 1; i1 < str.length; i1++) {
                    String base = str[i1].trim();
                    if (base.startsWith("-"))
                        base = base.substring(1);
                    base = base.trim();
                    actualBit.add(new StyledText(base+" ","afterline"));
                }
                actualBit.add(new StyledText(")\n","afterline"));
            }
        }
        return actualBit;
    }
}
