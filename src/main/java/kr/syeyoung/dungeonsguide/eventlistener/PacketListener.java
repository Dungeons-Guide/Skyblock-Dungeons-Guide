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

package kr.syeyoung.dungeonsguide.eventlistener;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.cosmetics.CustomPacketPlayerListItem;
import kr.syeyoung.dungeonsguide.events.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.events.PlayerListItemPacketEvent;
import kr.syeyoung.dungeonsguide.events.TitleEvent;
import kr.syeyoung.dungeonsguide.events.WindowUpdateEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

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
        if (packet instanceof  S30PacketWindowItems || packet instanceof S2FPacketSetSlot) {
            MinecraftForge.EVENT_BUS.post(new WindowUpdateEvent(packet instanceof  S30PacketWindowItems ? (S30PacketWindowItems)packet : null , packet instanceof S2FPacketSetSlot ? (S2FPacketSetSlot)packet : null));
        }
        super.channelRead(ctx, packet);
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
