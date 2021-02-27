package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonFairySoul;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.DungeonEndListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FeatureWatcherWarning extends TextHUDFeature implements ChatListener, DungeonEndListener {

    public FeatureWatcherWarning() {
        super("Dungeon","Watcher Spawn Alert", "Alert when watcher says 'That will be enough for now'", "dungen.watcherwarn", true, getFontRenderer().getStringWidth("Watcher finished spawning all mobs!"), getFontRenderer().FONT_HEIGHT);
        getStyles().add(new TextStyle("warning", new AColor(0xFF, 0x69,0x17,255), new AColor(0, 0,0,0), false));
        setEnabled(false);
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public boolean isHUDViewable() {
        return warning > System.currentTimeMillis();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Collections.singletonList("warning");
    }

    private UUID lastRoomUID = UUID.randomUUID();
    private long warning = 0;

    private static final List<StyledText> text = new ArrayList<StyledText>();
    static {
        text.add(new StyledText("Watcher finished spawning all mobs!", "warning"));
    }

    @Override
    public List<StyledText> getText() {
        return text;
    }

    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.message.getFormattedText().equals("§r§c[BOSS] The Watcher§r§f: That will be enough for now.§r"))  {
            warning = System.currentTimeMillis() + 2500;
            DungeonContext context = skyblockStatus.getContext();
            if (context ==null) return;
            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                if (dungeonRoom != null && dungeonRoom.getColor() == 18)
                    dungeonRoom.setCurrentState(DungeonRoom.RoomState.DISCOVERED);
            }
        }
    }

    @Override
    public void onDungeonEnd() {
        warning = 0;
    }
}
