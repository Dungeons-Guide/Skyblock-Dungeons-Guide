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

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.events.impl.PacketProcessedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RawPacketReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// called from ConnectionMixin before minecraft or any other mod handles the packet,
// so dg sees exactly what the server sent
public class PacketInjector {

    private static final Set<Class> targetedPackets = new HashSet<>();
    // S04 was only there for the spirit boots fix (D107), chunk packets only for block updates, now ClientChunkCacheMixin (D105)
    static {
        targetedPackets.add(ClientboundSetTitleTextPacket.class); // S45PacketTitle
        targetedPackets.add(ClientboundSetSubtitleTextPacket.class); // S45PacketTitle
        targetedPackets.add(ClientboundSetTitlesAnimationPacket.class); // S45PacketTitle
        targetedPackets.add(ClientboundClearTitlesPacket.class); // S45PacketTitle
        targetedPackets.add(ClientboundPlayerInfoUpdatePacket.class); // S38PacketPlayerListItem
        targetedPackets.add(ClientboundPlayerInfoRemovePacket.class); // S38PacketPlayerListItem
        targetedPackets.add(ClientboundContainerSetContentPacket.class); // S30PacketWindowItems
        targetedPackets.add(ClientboundContainerSetSlotPacket.class); // S2FPacketSetSlot
        targetedPackets.add(ClientboundBlockUpdatePacket.class); // S23PacketBlockChange
        targetedPackets.add(ClientboundSectionBlocksUpdatePacket.class); // S22PacketMultiBlockChange
        targetedPackets.add(ClientboundSetObjectivePacket.class); // S3BPacketScoreboardObjective
        targetedPackets.add(ClientboundSetScorePacket.class); // S3CPacketUpdateScore
        targetedPackets.add(ClientboundResetScorePacket.class); // S3CPacketUpdateScore
        targetedPackets.add(ClientboundSetDisplayObjectivePacket.class); // S3DPacketDisplayScoreboard
        targetedPackets.add(ClientboundSetPlayerTeamPacket.class); // S3EPacketTeams
        targetedPackets.add(ClientboundMapItemDataPacket.class); // S34PacketMaps
        targetedPackets.add(ClientboundRemoveEntitiesPacket.class); // S13PacketDestroyEntities
        targetedPackets.add(ClientboundTakeItemEntityPacket.class); // S0DPacketCollectItem
        targetedPackets.add(ClientboundBossEventPacket.class); // BossStatus
    }

    // netty thread. returns the packet minecraft should handle
    public static Packet<?> onPacketReceived(Packet<?> packet) {
        if (packet instanceof ClientboundBundlePacket bundlePacket) {
            boolean replaced = false;
            List<Packet<? super ClientGamePacketListener>> subPackets = new ArrayList<>();
            for (Packet<? super ClientGamePacketListener> subPacket : bundlePacket.subPackets()) {
                Packet<? super ClientGamePacketListener> result = (Packet<? super ClientGamePacketListener>) onPacketReceived(subPacket);
                if (result != subPacket) replaced = true;
                subPackets.add(result);
            }
            return replaced ? new ClientboundBundlePacket(subPackets) : packet;
        }

        if (!targetedPackets.contains(packet.getClass())) return packet;
        try {
            RawPacketReceivedEvent receivedEvent = new RawPacketReceivedEvent(packet);
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(receivedEvent);
            packet = receivedEvent.packet;
        } catch (Exception t) {
            FeatureCollectDiagnostics.queueSendLogAsync(t);
            t.printStackTrace();
        }

        Packet<?> finalPacket = packet;
        Minecraft.getInstance().execute(() -> {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new PacketProcessedEvent.Pre(finalPacket));
        });
        return packet;
    }

    // netty thread, after minecraft scheduled handling the packet
    public static void afterPacketReceived(Packet<?> packet) {
        if (packet instanceof ClientboundBundlePacket bundlePacket) {
            for (Packet<? super ClientGamePacketListener> subPacket : bundlePacket.subPackets())
                afterPacketReceived(subPacket);
            return;
        }
        if (!targetedPackets.contains(packet.getClass())) return;
        Minecraft.getInstance().execute(() -> {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new PacketProcessedEvent.Post(packet));
        });
    }
}
