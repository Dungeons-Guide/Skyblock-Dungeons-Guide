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

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.events.impl.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.map.MapDataManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Objective;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.NameTagVisibility;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.Team;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.TeamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Tuple;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PacketListener {
    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    @SubscribeEvent
    public void onPacketReceive(RawPacketReceivedEvent event) {
        Packet packet = event.packet;
        if (skyblockStatus.isOnSkyblock()
                && packet instanceof S04PacketEntityEquipment
                    && FeatureRegistry.FIX_SPIRIT_BOOTS.isEnabled()) { // Inventory packet name
            S04PacketEntityEquipment packet2 = (S04PacketEntityEquipment) packet;
            if (packet2.getEntityID() == Minecraft.getMinecraft().thePlayer.getEntityId()) {
                packet2 = new S04PacketEntityEquipment(packet2.getEntityID(), packet2.getEquipmentSlot() + 1, packet2.getItemStack());
                packet = packet2;
            }
        }
        event.packet = packet;
    }

    @SubscribeEvent
    public void packetProcessPost(PacketProcessedEvent.Post post) {
        Packet packet = post.packet;
        if (packet instanceof S30PacketWindowItems) {
            MinecraftForge.EVENT_BUS.post(new WindowUpdateEvent((S30PacketWindowItems) packet, null));
        } else if (packet instanceof S2FPacketSetSlot) {
            MinecraftForge.EVENT_BUS.post(new WindowUpdateEvent( null, (S2FPacketSetSlot) packet));
        } else if (packet instanceof S23PacketBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
            blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(
                    ((S23PacketBlockChange) packet).getBlockPosition(), ((S23PacketBlockChange) packet).getBlockState()));
            MinecraftForge.EVENT_BUS.post(blockUpdateEvent);
        } else if (packet instanceof S22PacketMultiBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : ((S22PacketMultiBlockChange) packet).getChangedBlocks()) {
                blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(changedBlock.getPos(), changedBlock.getBlockState()));
            }
            MinecraftForge.EVENT_BUS.post(blockUpdateEvent);
        }else if (packet instanceof S45PacketTitle) {
            MinecraftForge.EVENT_BUS.post(new TitleEvent((S45PacketTitle) packet));
        } else if (packet instanceof S38PacketPlayerListItem) {
            MinecraftForge.EVENT_BUS.post(new PlayerListItemPacketEvent((S38PacketPlayerListItem) packet));
        }else if (packet instanceof S34PacketMaps) {
            MapData mapData = MapDataManager.INSTANCE.createMapData(((S34PacketMaps) packet).getMapId());
            ((S34PacketMaps) packet).setMapdataTo(mapData);
            MinecraftForge.EVENT_BUS.post(new MapUpdateEvent(((S34PacketMaps) packet).getMapId(), mapData));
        }
    }

    @SubscribeEvent
    public void onPrePacketProcess(PacketProcessedEvent.Pre event) {
        Packet packet =event.packet;
        if (event.packet instanceof S23PacketBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
            blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(
                    ((S23PacketBlockChange) event.packet).getBlockPosition(),
                    ((S23PacketBlockChange) event.packet).getBlockState()));
            MinecraftForge.EVENT_BUS.post(blockUpdateEvent);
        } else if (event.packet instanceof S22PacketMultiBlockChange) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : ((S22PacketMultiBlockChange) event.packet).getChangedBlocks()) {
                blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(changedBlock.getPos(), changedBlock.getBlockState()));
            }
            MinecraftForge.EVENT_BUS.post(blockUpdateEvent);
        } else if (packet instanceof S3BPacketScoreboardObjective) {
            S3BPacketScoreboardObjective objectivePkt = (S3BPacketScoreboardObjective) packet;
            if (objectivePkt.func_149338_e() == 2) {
                Objective objective = ScoreboardManager.INSTANCE.getObjective(objectivePkt.func_149339_c());
                if (objective != null) {
                    objective.setDisplayName(objectivePkt.func_149337_d());
                    objective.setDisplayType(objectivePkt.func_179817_d());
                }
            } else if (objectivePkt.func_149338_e() == 1) {
                ScoreboardManager.INSTANCE.removeObjective(objectivePkt.func_149339_c());
            } else if (objectivePkt.func_149338_e() == 0) {
                Objective objective = new Objective(objectivePkt.func_149339_c());
                objective.setDisplayName(objectivePkt.func_149337_d());
                objective.setDisplayType(objectivePkt.func_179817_d());
                ScoreboardManager.INSTANCE.addObjective(objective);
            }
        } else if (packet instanceof S3CPacketUpdateScore) {
            S3CPacketUpdateScore score = (S3CPacketUpdateScore) packet;
            Objective objective = ScoreboardManager.INSTANCE.getObjective(score.getObjectiveName());
            if (objective != null) {
                if (score.getScoreAction() == S3CPacketUpdateScore.Action.CHANGE) {
                    objective.updateScore(score.getPlayerName(), score.getScoreValue());
                } else if (score.getScoreAction() == S3CPacketUpdateScore.Action.REMOVE) {
                    objective.removeScore(score.getPlayerName());
                }
            }
        } else if (packet instanceof S3DPacketDisplayScoreboard) {
            S3DPacketDisplayScoreboard board = (S3DPacketDisplayScoreboard) packet;
            ScoreboardManager.INSTANCE.displayScoreboard(board.func_149371_c(), board.func_149370_d());
        } else if (packet instanceof S3EPacketTeams) {
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
                    TabListEntry neu = new TabListEntry(entry1.getGameProfile(), entry.getGameMode());
                    neu.setPing(entry1.getPing());
                    neu.setDisplayName(entry1.getDisplayName());

                    TabList.INSTANCE.updateEntry(neu);
                }
            }
        }
    }

}
