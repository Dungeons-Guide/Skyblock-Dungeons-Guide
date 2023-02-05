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

package kr.syeyoung.dungeonsguide.mod.events.listener;

import io.netty.channel.*;
import kr.syeyoung.dungeonsguide.mod.events.impl.PacketProcessedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RawPacketReceivedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraftforge.common.MinecraftForge;
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
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        boolean doStuff = targetedPackets.contains(msg.getClass());
        try {
            if (doStuff) {
                RawPacketReceivedEvent receivedEvent = new RawPacketReceivedEvent(packet);
                MinecraftForge.EVENT_BUS.post(receivedEvent);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // Hopefully this works? idk
        if (doStuff)
            Minecraft.getMinecraft().addScheduledTask(() -> {
                MinecraftForge.EVENT_BUS.post(new PacketProcessedEvent.Pre(packet));
            });
        super.channelRead(ctx, packet);
        if (doStuff)
            Minecraft.getMinecraft().addScheduledTask(() -> {
                MinecraftForge.EVENT_BUS.post(new PacketProcessedEvent.Post(packet));
            });
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

    private ChannelPipeline thePipeline;
    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        (thePipeline =event.manager.channel().pipeline()).addBefore("packet_handler", "dg_packet_handler", this);
    }

    public void cleanup() {
        try {
            if (thePipeline != null)
                thePipeline.remove("dg_packet_handler");
        } catch (Exception e) {

        }
    }
}
