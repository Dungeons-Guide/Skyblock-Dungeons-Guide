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

package kr.syeyoung.dungeonsguide.mod.cosmetics;


import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerListItemPacketEvent;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraftforge.common.MinecraftForge;

public class CustomPacketPlayerListItem extends S38PacketPlayerListItem {
    public CustomPacketPlayerListItem(S38PacketPlayerListItem packet) {
        super(packet.getAction());
        getEntries().addAll(packet.getEntries());
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        super.processPacket(handler);

        MinecraftForge.EVENT_BUS.post(new PlayerListItemPacketEvent(this));
    }
}
