package kr.syeyoung.dungeonsguide.features.impl.secret;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureFreezePathfind extends SimpleFeature  {
    public FeatureFreezePathfind() {
        super("Secret", "Freeze Pathfind", "Freeze Pathfind, meaning the pathfind lines won't change when you move", "secret.freezepathfind", false);
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
}
