/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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
package kr.syeyoung.dungeonsguide.mod.events.impl;

import kr.syeyoung.dungeonsguide.mod.events.DGEvent;
import lombok.Getter;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

import java.util.EnumSet;

/**
 * Vanilla's tab list entries changing, fired by ClientPacketListenerMixin after vanilla applied the packet.
 */
public abstract class TabListEntryEvent extends DGEvent {
    @Getter
    private final PlayerInfo playerInfo;

    protected TabListEntryEvent(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    // a player new to the tab list
    public static class Add extends TabListEntryEvent {
        public Add(PlayerInfo playerInfo) {
            super(playerInfo);
        }
    }

    // a player removed from the tab list
    public static class Remove extends TabListEntryEvent {
        public Remove(PlayerInfo playerInfo) {
            super(playerInfo);
        }
    }

    // an existing entry updated by the packet's actions
    public static class Update extends TabListEntryEvent {
        @Getter
        private final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions;

        public Update(PlayerInfo playerInfo, EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions) {
            super(playerInfo);
            this.actions = actions;
        }
    }
}
