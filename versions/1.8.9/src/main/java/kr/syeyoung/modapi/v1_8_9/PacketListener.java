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

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.Pair;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityItem;
import kr.syeyoung.modapi.event.events.BlockUpdateEvent;
import kr.syeyoung.modapi.event.events.EntityExitWorldEvent;
import kr.syeyoung.modapi.event.events.ItemPickupEvent;
import kr.syeyoung.modapi.v1_8_9.world.BlockStateRegistryImpl;
import net.minecraft.block.Block;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;

public class PacketListener {

    public Packet  onPacketReceive(Packet packet) { // this runs async.
        return packet;
    }

    public void packetProcessPost(Packet packet) {  // this runs in sync.
        if (packet instanceof S23PacketBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
            BlockPos blockPosition = ((S23PacketBlockChange) packet).getBlockPosition();
            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
            blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(
                    new VectorI3D(
                            blockPosition.getX(),
                            blockPosition.getY(),
                            blockPosition.getZ()
                    ), impl.getByStateId(Block.getStateId(((S23PacketBlockChange) packet).getBlockState()))));
            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
        } else if (packet instanceof S22PacketMultiBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : ((S22PacketMultiBlockChange) packet).getChangedBlocks()) {
                blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(
                        new VectorI3D(changedBlock.getPos().getX(), changedBlock.getPos().getY(), changedBlock.getPos().getZ())
                        , impl.getByStateId(Block.getStateId(changedBlock.getBlockState()))));
            }
            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
        }
    }

    public void onPrePacketProcess(Packet packet) { // this runs in sync.
        if (packet instanceof S23PacketBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
            BlockPos blockPosition = ((S23PacketBlockChange) packet).getBlockPosition();
            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
            blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(
                    new VectorI3D(
                            blockPosition.getX(),
                            blockPosition.getY(),
                            blockPosition.getZ()
                    ), impl.getByStateId(Block.getStateId(((S23PacketBlockChange) packet).getBlockState()))));
            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
        } else if (packet instanceof S22PacketMultiBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : ((S22PacketMultiBlockChange) packet).getChangedBlocks()) {
                blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(new VectorI3D(
                        changedBlock.getPos().getX(),
                        changedBlock.getPos().getY(),
                        changedBlock.getPos().getZ()
                ), impl.getByStateId(Block.getStateId(changedBlock.getBlockState()))));
            }
            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
        } else if (packet instanceof S0DPacketCollectItem) {
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
