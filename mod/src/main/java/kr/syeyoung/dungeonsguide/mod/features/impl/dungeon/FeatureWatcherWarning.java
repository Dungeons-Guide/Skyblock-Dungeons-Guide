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
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.mod.features.listener.DungeonEndListener;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FeatureWatcherWarning extends TextHUDFeature implements ChatListener, DungeonEndListener {

    public FeatureWatcherWarning() {
        super("Dungeon.Blood Room","Watcher Spawn Alert", "Alert when watcher says 'That will be enough for now'", "dungen.watcherwarn", true, getFontRenderer().getStringWidth("Watcher finished spawning all mobs!"), getFontRenderer().FONT_HEIGHT);
        getStyles().add(new TextStyle("warning", new AColor(0xFF, 0x69,0x17,255), new AColor(0, 0,0,0), false));
        setEnabled(false);
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public boolean isHUDViewable() {
        return warning > System.currentTimeMillis();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Collections.singletonList("warning");
    }

    private final UUID lastRoomUID = UUID.randomUUID();
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
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
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
