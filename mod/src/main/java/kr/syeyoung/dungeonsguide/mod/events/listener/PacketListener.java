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
import kr.syeyoung.dungeonsguide.mod.events.impl.*;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.map.MapDataManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Objective;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.NameTagVisibility;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.Team;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.TeamManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import kr.syeyoung.dungeonsguide.mod.events.annotations.SubscribeEvent;

import java.util.*;

public class PacketListener {

    @SubscribeEvent
    public void packetProcessPost(PacketProcessedEvent.Post post) {
        Packet<?> packet = post.packet;
        if (packet instanceof ClientboundContainerSetContentPacket) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new WindowUpdateEvent((ClientboundContainerSetContentPacket) packet, null));
        } else if (packet instanceof ClientboundContainerSetSlotPacket) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new WindowUpdateEvent( null, (ClientboundContainerSetSlotPacket) packet));
        } else if (packet instanceof ClientboundBlockUpdatePacket) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
            blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(
                    ((ClientboundBlockUpdatePacket) packet).getPos(), ((ClientboundBlockUpdatePacket) packet).getBlockState()));
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(blockUpdateEvent);
        } else if (packet instanceof ClientboundSectionBlocksUpdatePacket) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
            ((ClientboundSectionBlocksUpdatePacket) packet).runUpdates((pos, state) -> blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(pos.immutable(), state)));
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(blockUpdateEvent);
        } else if (packet instanceof ClientboundSetTitleTextPacket || packet instanceof ClientboundSetSubtitleTextPacket
                || packet instanceof ClientboundSetTitlesAnimationPacket || packet instanceof ClientboundClearTitlesPacket) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new TitleEvent(packet));
        } else if (packet instanceof ClientboundPlayerInfoUpdatePacket) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new PlayerListItemPacketEvent((ClientboundPlayerInfoUpdatePacket) packet));
        } else if (packet instanceof ClientboundMapItemDataPacket) {
            ClientboundMapItemDataPacket mapPacket = (ClientboundMapItemDataPacket) packet;
            MapItemSavedData mapData = MapDataManager.INSTANCE.createMapData(mapPacket.mapId().id(), mapPacket.scale(), mapPacket.locked());
            try {
                mapPacket.applyToMap(mapData);
            } catch (Exception ignored) {} // hypixel seem to be sending bad map datas.
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new MapUpdateEvent(mapPacket.mapId().id(), mapData));
        }
    }

    // the parallel universe keeps what the server sent, so text is copied before vanilla or other mods get the packet (D106)
    private static Component copy(Component component) {
        return component == null ? null : TextUtils.copyComponent(component);
    }

    @SubscribeEvent
    public void onPrePacketProcess(PacketProcessedEvent.Pre event) {
        Packet<?> packet = event.packet;
        if (packet instanceof ClientboundBlockUpdatePacket) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
            blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(
                    ((ClientboundBlockUpdatePacket) packet).getPos(),
                    ((ClientboundBlockUpdatePacket) packet).getBlockState()));
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(blockUpdateEvent);
        } else if (packet instanceof ClientboundSectionBlocksUpdatePacket) {
            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
            ((ClientboundSectionBlocksUpdatePacket) packet).runUpdates((pos, state) -> blockUpdateEvent.getUpdatedBlocks().add(new Tuple<>(pos.immutable(), state)));
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(blockUpdateEvent);
        } else if (packet instanceof ClientboundSetObjectivePacket) {
            ClientboundSetObjectivePacket objectivePkt = (ClientboundSetObjectivePacket) packet;
            if (objectivePkt.getMethod() == ClientboundSetObjectivePacket.METHOD_CHANGE) {
                Objective objective = ScoreboardManager.INSTANCE.getObjective(objectivePkt.getObjectiveName());
                if (objective != null) {
                    objective.setDisplayName(copy(objectivePkt.getDisplayName()));
                    objective.setDisplayType(objectivePkt.getRenderType());
                    objective.setNumberFormat(objectivePkt.getNumberFormat().orElse(null));
                }
            } else if (objectivePkt.getMethod() == ClientboundSetObjectivePacket.METHOD_REMOVE) {
                ScoreboardManager.INSTANCE.removeObjective(objectivePkt.getObjectiveName());
            } else if (objectivePkt.getMethod() == ClientboundSetObjectivePacket.METHOD_ADD) {
                Objective objective = new Objective(objectivePkt.getObjectiveName());
                objective.setDisplayName(copy(objectivePkt.getDisplayName()));
                objective.setDisplayType(objectivePkt.getRenderType());
                objective.setNumberFormat(objectivePkt.getNumberFormat().orElse(null));
                ScoreboardManager.INSTANCE.addObjective(objective);
            }
        } else if (packet instanceof ClientboundSetScorePacket) {
            ClientboundSetScorePacket score = (ClientboundSetScorePacket) packet;
            Objective objective = ScoreboardManager.INSTANCE.getObjective(score.objectiveName());
            if (objective != null) {
                objective.updateScore(score.owner(), score.score(), copy(score.display().orElse(null)), score.numberFormat().orElse(null));
            }
        } else if (packet instanceof ClientboundResetScorePacket) {
            // 1.8.9's S3C REMOVE. no objective means every objective
            ClientboundResetScorePacket reset = (ClientboundResetScorePacket) packet;
            if (reset.objectiveName() == null) {
                ScoreboardManager.INSTANCE.removeScoreFromAll(reset.owner());
            } else {
                Objective objective = ScoreboardManager.INSTANCE.getObjective(reset.objectiveName());
                if (objective != null) objective.removeScore(reset.owner());
            }
        } else if (packet instanceof ClientboundSetDisplayObjectivePacket) {
            ClientboundSetDisplayObjectivePacket board = (ClientboundSetDisplayObjectivePacket) packet;
            ScoreboardManager.INSTANCE.displayScoreboard(board.getSlot().id(), board.getObjectiveName());
        } else if (packet instanceof ClientboundSetPlayerTeamPacket) {
            ClientboundSetPlayerTeamPacket pkt = (ClientboundSetPlayerTeamPacket) packet;
            // 1.8.9 actions: 0 create, 1 remove, 2 update, 3 player add, 4 player remove
            if (pkt.getTeamAction() == ClientboundSetPlayerTeamPacket.Action.ADD) {
                // CREATE
                Team team = new Team(pkt.getName());
                if (pkt.getParameters().isPresent()) {
                    ClientboundSetPlayerTeamPacket.Parameters parameters = pkt.getParameters().get();
                    team.setDisplayName(copy(parameters.getDisplayName()));
                    team.setPrefix(copy(parameters.getPlayerPrefix()));
                    team.setSuffix(copy(parameters.getPlayerSuffix()));
                    team.setNameTagVisibility(NameTagVisibility.of(parameters.getNametagVisibility().getSerializedName()));
                    team.setColor(parameters.getColor());
                }

                for (String player : pkt.getPlayers()) {
                    team.addTeamMember(player);
                }

                TeamManager.INSTANCE.createTeam(team);
            } else if (pkt.getTeamAction() == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
                // REMOVE
                TeamManager.INSTANCE.removeTeam(pkt.getName());
            } else if (pkt.getParameters().isPresent()) {
                // UPDATE
                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
                if (team != null) {
                    ClientboundSetPlayerTeamPacket.Parameters parameters = pkt.getParameters().get();
                    team.setDisplayName(copy(parameters.getDisplayName()));
                    team.setPrefix(copy(parameters.getPlayerPrefix()));
                    team.setSuffix(copy(parameters.getPlayerSuffix()));
                    team.setNameTagVisibility(NameTagVisibility.of(parameters.getNametagVisibility().getSerializedName()));
                    team.setColor(parameters.getColor());
                }
            } else if (pkt.getPlayerAction() == ClientboundSetPlayerTeamPacket.Action.ADD) {
                // PLAYER UPDATE
                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
                if (team != null) {
                    for (String player : pkt.getPlayers()) {
                        team.addTeamMember(player);
                    }
                }
            } else if (pkt.getPlayerAction() == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
                // PLAYER REMOVE
                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
                if (team != null) {
                    for (String player : pkt.getPlayers()) {
                        team.removeTeamMember(player);
                    }
                }
            }
        }  else if (packet instanceof ClientboundPlayerInfoUpdatePacket) {
            // 1.8.9 sent one action per packet, 26.1 sends a set of them
            ClientboundPlayerInfoUpdatePacket pkt = (ClientboundPlayerInfoUpdatePacket) packet;
            EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = pkt.actions();
            for (ClientboundPlayerInfoUpdatePacket.Entry entry : pkt.entries()) {
                if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                    // the profile is an immutable record on 26.1
                    TabListEntry tabListEntry = new TabListEntry(entry.profile(), entry.gameMode());
                    tabListEntry.setPing(entry.latency());
                    tabListEntry.setDisplayName(copy(entry.displayName()));
                    TabList.INSTANCE.updateEntry(tabListEntry);
                    continue;
                }
                TabListEntry existing = TabList.INSTANCE.getEntry(entry.profileId());
                if (existing == null) continue;
                if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY)) {
                    existing.setPing(entry.latency());
                }
                if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) {
                    existing.setDisplayName(copy(entry.displayName()));
                }
                if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE)) {
                    TabListEntry neu = new TabListEntry(existing.getGameProfile(), entry.gameMode());
                    neu.setPing(existing.getPing());
                    neu.setDisplayName(existing.getDisplayName());

                    TabList.INSTANCE.updateEntry(neu);
                }
            }
        } else if (packet instanceof ClientboundPlayerInfoRemovePacket) {
            // 1.8.9's S38 REMOVE_PLAYER
            for (UUID profileId : ((ClientboundPlayerInfoRemovePacket) packet).profileIds()) {
                TabList.INSTANCE.removeEntry(profileId);
            }
        } else if (packet instanceof ClientboundRemoveEntitiesPacket) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new EntityExitWorldEvent(((ClientboundRemoveEntitiesPacket) packet).getEntityIds().toIntArray()));
        } else if (packet instanceof ClientboundTakeItemEntityPacket) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new ItemPickupEvent(((ClientboundTakeItemEntityPacket) packet).getItemId(), ((ClientboundTakeItemEntityPacket) packet).getPlayerId()));
        }
    }
}
