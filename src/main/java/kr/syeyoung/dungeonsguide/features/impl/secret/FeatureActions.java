package kr.syeyoung.dungeonsguide.features.impl.secret;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureActions extends TextHUDFeature {
    public FeatureActions() {
        super("Secret", "Action Viewer", "View List of actions that needs to be taken", "secret.actionview", false, 200, getFontRenderer().FONT_HEIGHT * 10);
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    @Override
    public boolean doesScaleWithHeight() {
        return false;
    }

    @Override
    public boolean isHUDViewable() {
        if (Minecraft.getMinecraft().currentScreen != null) return false;
        if (!skyblockStatus.isOnDungeon()) return false;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return false;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return false;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return false;
        return true;
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
        return Arrays.asList(new String[] {
                "pathfinding","mechanic","separator","state","current", "number", "dot", "action", "afterline"
        });
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }


    @Override
    public List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();

        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);

        ActionRoute path = ((GeneralRoomProcessor)dungeonRoom.getRoomProcessor()).getPath();

        if (path != null) {
            actualBit.add(new StyledText("Pathfinding ","pathfinding"));
            actualBit.add(new StyledText(path.getMechanic()+" ","mechanic"));
            actualBit.add(new StyledText("-> ","separator"));
            actualBit.add(new StyledText(path.getState()+"\n","state"));

            for (int i = 0; i < path.getActions().size(); i++) {
                actualBit.add(new StyledText((i == path.getCurrent() ? ">" : " ") +" ","current"));
                actualBit.add(new StyledText(i+"","number"));
                actualBit.add(new StyledText(". ","dot"));
                Action action = path.getActions().get(i);
                String[] str = action.toString().split(" ");
                actualBit.add(new StyledText(str[0] + " ","action"));
                for (int i1 = 1; i1 < str.length; i1++) {
                    actualBit.add(new StyledText(str[i1]+" ","afterline"));
                }
                actualBit.add(new StyledText("\n","afterline"));
            }
        }
        return actualBit;
    }
}
