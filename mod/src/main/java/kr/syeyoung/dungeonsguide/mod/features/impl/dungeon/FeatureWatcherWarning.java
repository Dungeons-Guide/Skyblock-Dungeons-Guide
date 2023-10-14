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
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.UUID;

public class FeatureWatcherWarning extends TextHUDFeature {

    public FeatureWatcherWarning() {
        super("Dungeon HUD.Alerts","Watcher Spawn Alert", "Alert when watcher says 'That will be enough for now'", "dungen.watcherwarn");
        registerDefaultStyle("warning", DefaultingDelegatingTextStyle.derive("Feature Default - Warning", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.WARNING)));
        setEnabled(false);
    }

    @Override
    public boolean isHUDViewable() {
        return warning > System.currentTimeMillis() && SkyblockStatus.isOnDungeon();
    }

    private final UUID lastRoomUID = UUID.randomUUID();
    private long warning = 0;

    @Override
    public TextSpan getText() {
        return new TextSpan(getStyle("warning"), "Watcher finished spawning all mobs!");
    }


    @DGEventHandler()
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.message.getFormattedText().equals("§r§c[BOSS] The Watcher§r§f: That will be enough for now.§r"))  {
            warning = System.currentTimeMillis() + 2500;
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context ==null) return;
            for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
                if (dungeonRoom != null && dungeonRoom.getColor() == 18)
                    dungeonRoom.setCurrentState(DungeonRoom.RoomState.DISCOVERED);
            }
        }
    }

    @DGEventHandler(ignoreDisabled = true, triggerOutOfSkyblock = true)
    public void onDungeonEnd(DungeonLeftEvent event) {
        warning = 0;
    }
}
