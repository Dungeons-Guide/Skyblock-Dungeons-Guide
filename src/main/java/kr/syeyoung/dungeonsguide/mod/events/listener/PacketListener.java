/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.events.listener;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.tab.CustomPacketPlayerListItem;
import kr.syeyoung.dungeonsguide.mod.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.TitleEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.WindowUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.Arrays;

@ChannelHandler.Sharable
public class PacketListener extends ChannelDuplexHandler {
    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        if (skyblockStatus.isOnSkyblock()
                && msg instanceof S04PacketEntityEquipment
                && FeatureRegistry.FIX_SPIRIT_BOOTS.isEnabled()) { // Inventory packet name
            S04PacketEntityEquipment packet2 = (S04PacketEntityEquipment) msg;
            if (packet2.getEntityID() == Minecraft.getMinecraft().thePlayer.getEntityId()) {
                packet2 = new S04PacketEntityEquipment(packet2.getEntityID(), packet2.getEquipmentSlot() + 1, packet2.getItemStack());
                packet = packet2;
            }
        }
        if (packet instanceof S45PacketTitle) {
            MinecraftForge.EVENT_BUS.post(new TitleEvent((S45PacketTitle) packet));
        }
        if (packet instanceof S38PacketPlayerListItem) {
            packet = new CustomPacketPlayerListItem((S38PacketPlayerListItem) packet);
        }
        if (packet instanceof  S30PacketWindowItems) {
            packet = new CustomWindowItems((S30PacketWindowItems) packet);
        }
        if (packet instanceof S2FPacketSetSlot) {
            packet = new CustomSetSlot((S2FPacketSetSlot) packet);
        }
        if (packet instanceof S23PacketBlockChange) {
            packet = new SingleBlockChange((S23PacketBlockChange) packet);
        } else if (packet instanceof S22PacketMultiBlockChange) {
            packet = new MultiBlockChange((S22PacketMultiBlockChange) packet);
        }
        super.channelRead(ctx, packet);
    }

    private static class CustomWindowItems extends S30PacketWindowItems {
        public CustomWindowItems(S30PacketWindowItems parent) {
            super(parent.func_148911_c(), Arrays.asList(parent.getItemStacks()));
        }

        @Override
        public void processPacket(INetHandlerPlayClient handler) {
            super.processPacket(handler);
            MinecraftForge.EVENT_BUS.post(new WindowUpdateEvent(this, null));
        }
    }
    private static class CustomSetSlot extends S2FPacketSetSlot {
        public CustomSetSlot(S2FPacketSetSlot parent) {
            super(parent.func_149175_c(), parent.func_149173_d(), parent.func_149174_e());
        }
        @Override
        public void processPacket(INetHandlerPlayClient handler) {
            super.processPacket(handler);
            MinecraftForge.EVENT_BUS.post(new WindowUpdateEvent(null, this));
        }
    }

    private static class SingleBlockChange extends S23PacketBlockChange {
        private S23PacketBlockChange old;
        public SingleBlockChange(S23PacketBlockChange blockChange) {
            this.old = blockChange;
        }

        @Override
        public void processPacket(INetHandlerPlayClient handler) {

            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
            blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(getBlockPosition(),getBlockState()));


             MinecraftForge.EVENT_BUS.post(blockUpdateEvent);
            super.processPacket(handler);
            blockUpdateEvent = new BlockUpdateEvent.Post();
            blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(getBlockPosition(), getBlockState()));
             MinecraftForge.EVENT_BUS.post(blockUpdateEvent);




        }

        @Override
        public BlockPos getBlockPosition() {
            return old.getBlockPosition();
        }

        @Override
        public IBlockState getBlockState() {
            return old.getBlockState();
        }
    }


    private static class MultiBlockChange extends S22PacketMultiBlockChange {
        private S22PacketMultiBlockChange old;
        public MultiBlockChange(S22PacketMultiBlockChange blockChange) {
            this.old = blockChange;
        }
        @Override
        public void processPacket(INetHandlerPlayClient handler) {


            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : getChangedBlocks()) {
                blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(changedBlock.getPos(), changedBlock.getBlockState()));
            }
             MinecraftForge.EVENT_BUS.post(blockUpdateEvent);
            super.processPacket(handler);
            blockUpdateEvent = new BlockUpdateEvent.Post();
            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : getChangedBlocks()) {
                blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(changedBlock.getPos(), changedBlock.getBlockState()));
            }
             MinecraftForge.EVENT_BUS.post(blockUpdateEvent);


        }

        @Override
        public BlockUpdateData[] getChangedBlocks() {
            return old.getChangedBlocks();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Packet packet = (Packet) msg;
        if (packet instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet2 = (C02PacketUseEntity) packet;
            PlayerInteractEntityEvent piee;
            if (packet2.getAction() == C02PacketUseEntity.Action.ATTACK)
                piee = new PlayerInteractEntityEvent(true, packet2.getEntityFromWorld(Minecraft.getMinecraft().theWorld));
            else
                piee = new PlayerInteractEntityEvent(false, ((C02PacketUseEntity) packet).getEntityFromWorld(Minecraft.getMinecraft().theWorld));

            if (MinecraftForge.EVENT_BUS.post(piee)) return;
        }
        super.write(ctx, msg, promise);
    }

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        event.manager.channel().pipeline().addBefore("packet_handler", "dg_packet_handler", this);
    }
}
