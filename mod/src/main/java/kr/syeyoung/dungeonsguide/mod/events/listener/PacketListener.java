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

package kr.syeyoung.dungeonsguide.mod.events.listener;

import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.events.impl.PacketProcessedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerListItemPacketEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RawPacketReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.NameTagVisibility;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.Team;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.TeamManager;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.util.EnumChatFormatting;

public class PacketListener {

    @SubscribeEvent
    public void onPacketReceive(RawPacketReceivedEvent event) {
        try {
            Packet packet = event.packet;
            if (SkyblockStatus.isOnSkyblock()
                    && packet instanceof S04PacketEntityEquipment
                    && FeatureRegistry.FIX_SPIRIT_BOOTS.isEnabled()) { // Inventory packet name
                S04PacketEntityEquipment packet2 = (S04PacketEntityEquipment) packet;
                if (Minecraft.getMinecraft().thePlayer != null && packet2.getEntityID() == Minecraft.getMinecraft().thePlayer.getEntityId()) {
                    packet2 = new S04PacketEntityEquipment(packet2.getEntityID(), packet2.getEquipmentSlot() + 1, packet2.getItemStack());
                    packet = packet2;
                }
            }
            event.packet = packet;
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void packetProcessPost(PacketProcessedEvent.Post post) {
        Packet packet = post.packet;
        if (packet instanceof S38PacketPlayerListItem) {
            ModAPI.getAPI().getEventBus().fireEvent(new PlayerListItemPacketEvent((S38PacketPlayerListItem) packet));
        }
    }

    @SubscribeEvent
    public void onPrePacketProcess(PacketProcessedEvent.Pre event) {
        Packet packet =event.packet;
        if (packet instanceof S3EPacketTeams) {
            S3EPacketTeams pkt = (S3EPacketTeams) packet;
            if (pkt.getAction() == 0) {
                // CREATE
                Team team = new Team(pkt.getName());
                team.setDisplayName(pkt.getDisplayName());
                team.setPrefix(pkt.getPrefix());
                team.setSuffix(pkt.getSuffix());
                team.setNameTagVisibility(NameTagVisibility.of(pkt.getNameTagVisibility()));
                team.setColor(EnumChatFormatting.func_175744_a(pkt.getColor()));

                for (String player : pkt.getPlayers()) {
                    team.addTeamMember(player);
                }

                TeamManager.INSTANCE.createTeam(team);
            } else if (pkt.getAction() == 1) {
                // REMOVE
                TeamManager.INSTANCE.removeTeam(pkt.getName());
            } else if (pkt.getAction() == 2) {
                // UPDATE
                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
                if (team != null) {
                    team.setDisplayName(pkt.getDisplayName());
                    team.setPrefix(pkt.getPrefix());
                    team.setSuffix(pkt.getSuffix());
                    team.setNameTagVisibility(NameTagVisibility.of(pkt.getNameTagVisibility()));
                    team.setColor(EnumChatFormatting.func_175744_a(pkt.getColor()));
                }
            } else if (pkt.getAction() == 3) {
                // PLAYER UPDATE
                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
                if (team != null) {
                    for (String player : pkt.getPlayers()) {
                        team.addTeamMember(player);
                    }
                }
            } else if (pkt.getAction() == 4) {
                // PLAYER REMOVE
                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
                if (team != null) {
                    for (String player : pkt.getPlayers()) {
                        team.removeTeamMember(player);
                    }
                }
            }
        }  else if (packet instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem pkt = (S38PacketPlayerListItem) packet;
            S38PacketPlayerListItem.Action action = pkt.getAction();
            if (action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
                    TabListEntry tabListEntry = new TabListEntry(entry.getProfile(), entry.getGameMode());
                    tabListEntry.setPing(entry.getPing());
                    tabListEntry.setDisplayName(entry.getDisplayName());
                    TabList.INSTANCE.updateEntry(tabListEntry);
                }
            } else if (action == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
                    TabList.INSTANCE.removeEntry(entry.getProfile().getId());
                }
            } else if (action == S38PacketPlayerListItem.Action.UPDATE_LATENCY) {
                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
                    TabListEntry entry1 = TabList.INSTANCE.getEntry(entry.getProfile().getId());
                    if (entry1 != null) entry1.setPing(entry.getPing());
                }
            } else if (action == S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME) {
                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
                    TabListEntry entry1 = TabList.INSTANCE.getEntry(entry.getProfile().getId());
                    if (entry1 != null) entry1.setDisplayName(entry.getDisplayName());
                }
            } else if (action == S38PacketPlayerListItem.Action.UPDATE_GAME_MODE) {
                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
                    TabListEntry entry1 = TabList.INSTANCE.getEntry(entry.getProfile().getId());
                    if (entry1 == null) return;
                    TabListEntry neu = new TabListEntry(entry1.getGameProfile(), entry.getGameMode());
                    neu.setPing(entry1.getPing());
                    neu.setDisplayName(entry1.getDisplayName());

                    TabList.INSTANCE.updateEntry(neu);
                }
            }
        }
    }

}
