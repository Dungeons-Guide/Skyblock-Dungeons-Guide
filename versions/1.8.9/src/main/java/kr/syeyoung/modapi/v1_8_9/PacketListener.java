/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.modapi.v1_8_9;

import kr.syeyoung.modapi.entity.UEntityItem;
import kr.syeyoung.modapi.event.events.EntityExitWorldEvent;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.event.events.ItemPickupEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.network.play.server.S13PacketDestroyEntities;

public class PacketListener {

    public Packet  onPacketReceive(Packet packet) { // this runs async.
        return packet;
    }

    public void packetProcessPost(Packet packet) {  // this runs in sync.
    }

    public void onPrePacketProcess(Packet packet) { // this runs in sync.
        if (packet instanceof S0DPacketCollectItem) {
            UEntity possiblyItem = ModAPI.getAPI().getWorld().getEntityById(((S0DPacketCollectItem) packet).getCollectedItemEntityID());
            UEntity possiblyPlayer = ModAPI.getAPI().getWorld().getEntityById(((S0DPacketCollectItem) packet).getEntityID());
            if (possiblyItem instanceof UEntityItem) {
                ModAPI.getAPI().getEventBus().fireEvent(new ItemPickupEvent(
                        possiblyItem,
                        possiblyPlayer
                ));
            }
        } else if (packet instanceof S13PacketDestroyEntities) {
            ModAPI.getAPI().getEventBus().fireEvent(new EntityExitWorldEvent(
                    ((S13PacketDestroyEntities) packet).getEntityIDs())
            );
        }
    }

}
