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

package kr.syeyoung.modapi.v1_8_9;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.modapi.ModAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.HashSet;
import java.util.Set;

@ChannelHandler.Sharable
public class PacketInjector extends ChannelDuplexHandler {

    private static Set<Class> targetedPackets = new HashSet<>();
    static {
        targetedPackets.add(S04PacketEntityEquipment.class);
        targetedPackets.add(S45PacketTitle.class);
        targetedPackets.add(S38PacketPlayerListItem.class);
        targetedPackets.add(S30PacketWindowItems.class);
        targetedPackets.add(S2FPacketSetSlot.class);
        targetedPackets.add(S23PacketBlockChange.class);
        targetedPackets.add(S22PacketMultiBlockChange.class);
        targetedPackets.add(S3BPacketScoreboardObjective.class);
        targetedPackets.add(S3CPacketUpdateScore.class);
        targetedPackets.add(S3DPacketDisplayScoreboard.class);
        targetedPackets.add(S3EPacketTeams.class);
        targetedPackets.add(S34PacketMaps.class);
        targetedPackets.add(S21PacketChunkData.class);
        targetedPackets.add(S26PacketMapChunkBulk.class);
        targetedPackets.add(S13PacketDestroyEntities.class);
        targetedPackets.add(S0DPacketCollectItem.class);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        boolean doStuff = targetedPackets.contains(msg.getClass());
        try {
            if (doStuff) {
                packet = listener.onPacketReceive(packet);
            }
        } catch (Exception t) {
            FeatureCollectDiagnostics.queueSendLogAsync(t);
            t.printStackTrace();
        }

        // Hopefully this works? idk
        Packet finalPacket = packet;
        if (doStuff)
            Minecraft.getMinecraft().addScheduledTask(() -> {
                listener.onPrePacketProcess(finalPacket);
            });
        super.channelRead(ctx, packet);
        if (doStuff)
            Minecraft.getMinecraft().addScheduledTask(() -> {
                listener.packetProcessPost(finalPacket);
            });
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Packet packet = (Packet) msg;
        if (packet instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet2 = (C02PacketUseEntity) packet;
            PlayerInteractEntityEvent piee;
            if (packet2.getAction() == C02PacketUseEntity.Action.ATTACK)
                piee = new PlayerInteractEntityEvent(true, false, packet2.getEntityFromWorld(Minecraft.getMinecraft().theWorld));
            else
                piee = new PlayerInteractEntityEvent(false, packet2.getAction() == C02PacketUseEntity.Action.INTERACT_AT, ((C02PacketUseEntity) packet).getEntityFromWorld(Minecraft.getMinecraft().theWorld));
            try {
                if (ModAPI.getAPI().getEventBus().fireEvent(piee)) return;
            } catch (Exception e) {
                FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
            }
        }
        super.write(ctx, msg, promise);
    }

    private PacketListener listener = new PacketListener();

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        event.manager.channel().pipeline().addBefore("packet_handler", "dg_packet_handler", this);
    }

    public void cleanup() {
        try {
            if (Minecraft.getMinecraft().getNetHandler() != null)
                Minecraft.getMinecraft().getNetHandler().getNetworkManager().channel().pipeline().remove("dg_packet_handler");
        } catch (Exception e) {

        }
    }
}
