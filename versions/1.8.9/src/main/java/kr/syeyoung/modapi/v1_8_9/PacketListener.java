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
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.v1_8_9.item.UItemStackImpl;
import kr.syeyoung.modapi.v1_8_9.map.MapDataManager;
import kr.syeyoung.modapi.v1_8_9.util.CustomNetworkPlayerInfo;
import kr.syeyoung.modapi.v1_8_9.world.BlockStateRegistryImpl;
import kr.syeyoung.modapi.v1_8_9.world.FakeWorld;
import kr.syeyoung.modapi.v1_8_9.world.UChunkImpl;
import kr.syeyoung.modapi.v1_8_9.world.UMapDataImpl;
import kr.syeyoung.modapi.world.UChunk;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.*;

public class PacketListener {

    public Packet  onPacketReceive(Packet packet) { // this runs async.
        return packet;
    }

    public void packetProcessPost(Packet packet) {  // this runs in sync.
        if (packet instanceof S30PacketWindowItems) {
            ItemStack[] stacks = ((S30PacketWindowItems) packet).getItemStacks();
            List<WindowUpdateEvent.SlotUpdate> updates = new ArrayList<>();
            for (int i = 0; i < stacks.length; i++) {
                updates.add(new WindowUpdateEvent.SlotUpdate(i, stacks[i] == null ? null : new UItemStackImpl(stacks[i])));
            }
            ModAPI.getAPI().getEventBus().fireEvent(new WindowUpdateEvent(((S30PacketWindowItems) packet).func_148911_c(), updates, false));
        } else if (packet instanceof S2FPacketSetSlot) {
            ItemStack stack = ((S2FPacketSetSlot) packet).func_149174_e();
            ModAPI.getAPI().getEventBus().fireEvent(new WindowUpdateEvent(((S2FPacketSetSlot) packet).func_149175_c(),
                    Collections.singletonList(new WindowUpdateEvent.SlotUpdate(((S2FPacketSetSlot) packet).func_149173_d(), stack == null ? null : new UItemStackImpl(stack))), true));
        } else if (packet instanceof S23PacketBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
            BlockPos blockPosition = ((S23PacketBlockChange) packet).getBlockPosition();
            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
            blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(
                    new VectorI3D(
                            blockPosition.getX(),
                            blockPosition.getY(),
                            blockPosition.getZ()
                    ), impl.getByStateId(Block.BLOCK_STATE_IDS.get(((S23PacketBlockChange) packet).getBlockState()))));
            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
        } else if (packet instanceof S22PacketMultiBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : ((S22PacketMultiBlockChange) packet).getChangedBlocks()) {
                blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(
                        new VectorI3D(changedBlock.getPos().getX(), changedBlock.getPos().getY(), changedBlock.getPos().getZ())
                        , impl.getByStateId(Block.BLOCK_STATE_IDS.get(changedBlock.getBlockState()))));
            }
            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
        }else if (packet instanceof S21PacketChunkData) {
            try {
                if (((S21PacketChunkData) packet).getExtractedSize() == 0) return;
                WorldProvider provider = new WorldProviderSurface();
                if (Minecraft.getMinecraft().theWorld != null) provider = Minecraft.getMinecraft().theWorld.provider;
                BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();

                Chunk c = new Chunk(new FakeWorld(provider), ((S21PacketChunkData) packet).getChunkX(), ((S21PacketChunkData) packet).getChunkZ());
                c.fillChunk(((S21PacketChunkData) packet).getExtractedDataBytes(), ((S21PacketChunkData) packet).getExtractedSize(), ((S21PacketChunkData) packet).func_149274_i());
                ChunkUpdateEvent.Post chunkUpdateEvent = new ChunkUpdateEvent.Post(Collections.singletonList(new UChunkImpl(c, impl)));
                ModAPI.getAPI().getEventBus().fireEvent(chunkUpdateEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (packet instanceof S26PacketMapChunkBulk) {
            try {
                WorldProvider provider = new WorldProviderSurface();
                if (Minecraft.getMinecraft().theWorld != null) provider = Minecraft.getMinecraft().theWorld.provider;
                BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();

                List<UChunk> set = new ArrayList<>();
                for (int i = 0; i < ((S26PacketMapChunkBulk) packet).getChunkCount(); i++) {
                    Chunk c = new Chunk(new FakeWorld(provider), ((S26PacketMapChunkBulk) packet).getChunkX(i), ((S26PacketMapChunkBulk) packet).getChunkZ(i));
                    c.fillChunk(((S26PacketMapChunkBulk) packet).getChunkBytes(i), ((S26PacketMapChunkBulk) packet).getChunkSize(i), true);
                    set.add(new UChunkImpl(c, impl));
                }
                ChunkUpdateEvent.Post chunkUpdateEvent = new ChunkUpdateEvent.Post(set);
                ModAPI.getAPI().getEventBus().fireEvent(chunkUpdateEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (packet instanceof S34PacketMaps) {
            MapData mapData = MapDataManager.INSTANCE.createMapData(((S34PacketMaps) packet).getMapId());
            try {
                ((S34PacketMaps) packet).setMapdataTo(mapData);
            } catch (Exception ignored) {} // hypixel seem to be sending bad map datas.
            ModAPI.getAPI().getEventBus().fireEvent(new MapUpdateEvent(((S34PacketMaps) packet).getMapId(), new UMapDataImpl(mapData)));
        } else if (packet instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem p = (S38PacketPlayerListItem) packet;
            Map<UUID, NetworkPlayerInfo> playerInfoMap = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, Minecraft.getMinecraft().getNetHandler(), "playerInfoMap", "field_147310_i","i");
            if (p.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                for (S38PacketPlayerListItem.AddPlayerData entry : p.getEntries()) {
                    playerInfoMap.remove(entry.getProfile().getId());
                    playerInfoMap.put(entry.getProfile().getId(), new CustomNetworkPlayerInfo(entry));
                }
            }
        } else if (packet instanceof S45PacketTitle) {
            S45PacketTitle title = (S45PacketTitle) packet;
            TitleEvent titleEvent = new TitleEvent(
                    title.getType() == S45PacketTitle.Type.TITLE ? TitleEvent.Type.TITLE :
                            title.getType() == S45PacketTitle.Type.SUBTITLE ? TitleEvent.Type.SUBTITLE :
                                    title.getType() == S45PacketTitle.Type.RESET ? TitleEvent.Type.RESET :
                                            title.getType() == S45PacketTitle.Type.CLEAR ? TitleEvent.Type.CLEAR :
                                                    TitleEvent.Type.TIMES,
                    title.getMessage() == null ? null : GsonComponentSerializer.colorDownsamplingGson().deserialize(IChatComponent.Serializer.componentToJson(title.getMessage())),
                    title.getFadeInTime(),
                    title.getDisplayTime(),
                    title.getFadeOutTime()
            );
            ModAPI.getAPI().getEventBus().fireEvent(titleEvent);
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
                    ), impl.getByStateId(Block.BLOCK_STATE_IDS.get(((S23PacketBlockChange) packet).getBlockState()))));
            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
        } else if (packet instanceof S22PacketMultiBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : ((S22PacketMultiBlockChange) packet).getChangedBlocks()) {
                blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(new VectorI3D(
                        changedBlock.getPos().getX(),
                        changedBlock.getPos().getY(),
                        changedBlock.getPos().getZ()
                ), impl.getByStateId(Block.BLOCK_STATE_IDS.get(changedBlock.getBlockState()))));
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
        } else if (packet instanceof S21PacketChunkData) {
            try {
                if (((S21PacketChunkData) packet).getExtractedSize() == 0) return;
                WorldProvider provider = new WorldProviderSurface();
                if (Minecraft.getMinecraft().theWorld != null) provider = Minecraft.getMinecraft().theWorld.provider;
                BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();

                Chunk c = new Chunk(new FakeWorld(provider), ((S21PacketChunkData) packet).getChunkX(), ((S21PacketChunkData) packet).getChunkZ());
                c.fillChunk(((S21PacketChunkData) packet).getExtractedDataBytes(), ((S21PacketChunkData) packet).getExtractedSize(), ((S21PacketChunkData) packet).func_149274_i());
                ChunkUpdateEvent.Pre chunkUpdateEvent = new ChunkUpdateEvent.Pre(Collections.singletonList(new UChunkImpl(c, impl)));
                ModAPI.getAPI().getEventBus().fireEvent(chunkUpdateEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (packet instanceof S26PacketMapChunkBulk) {
            try {
                WorldProvider provider = new WorldProviderSurface();
                if (Minecraft.getMinecraft().theWorld != null) provider = Minecraft.getMinecraft().theWorld.provider;
                BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();

                List<UChunk> set = new ArrayList<>();
                for (int i = 0; i < ((S26PacketMapChunkBulk) packet).getChunkCount(); i++) {
                    Chunk c = new Chunk(new FakeWorld(provider), ((S26PacketMapChunkBulk) packet).getChunkX(i), ((S26PacketMapChunkBulk) packet).getChunkZ(i));
                    c.fillChunk(((S26PacketMapChunkBulk) packet).getChunkBytes(i), ((S26PacketMapChunkBulk) packet).getChunkSize(i), true);
                    set.add(new UChunkImpl(c, impl));
                }
                ChunkUpdateEvent.Pre chunkUpdateEvent = new ChunkUpdateEvent.Pre(set);
                ModAPI.getAPI().getEventBus().fireEvent(chunkUpdateEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
