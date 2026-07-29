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

package kr.syeyoung.modapi.v1_21_9;

import kr.syeyoung.modapi.v1_21_9.paralleluniverse.scoreboard.Objective;
import kr.syeyoung.modapi.v1_21_9.paralleluniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.modapi.v1_21_9.util.TextUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;

public class PacketListener {
    public static final PacketListener INSTANCE = new PacketListener();

    public void init() {

    }

    public void onPacketReceive(Packet packet) {
        if (packet instanceof ScoreboardObjectiveUpdateS2CPacket objectivePkt) {
            if (objectivePkt.getMode() == 2) {
                Objective objective = ScoreboardManager.INSTANCE.getObjective(objectivePkt.getName());
                if (objective != null) {
                    objective.setDisplayName(TextUtils.fromText(objectivePkt.getDisplayName()));
                    objective.setDisplayType(objectivePkt.getType());
                }
            } else if (objectivePkt.getMode() == 1) {
                ScoreboardManager.INSTANCE.removeObjective(objectivePkt.getName());
            } else if (objectivePkt.getMode() == 0) {
                Objective objective = new Objective(objectivePkt.getName());
                objective.setDisplayName(TextUtils.fromText(objectivePkt.getDisplayName()));
                objective.setDisplayType(objectivePkt.getType());
                ScoreboardManager.INSTANCE.addObjective(objective);
            }
        } else if (packet instanceof ScoreboardScoreUpdateS2CPacket score) {
            Objective objective = ScoreboardManager.INSTANCE.getObjective(score.objectiveName());
            if (objective != null) {
                objective.updateScore(score.scoreHolderName(), score.display().map(TextUtils::fromText).orElse(null), score.score());
            }
        } else if (packet instanceof ScoreboardDisplayS2CPacket board) {
            ScoreboardManager.INSTANCE.displayScoreboard(board.getSlot(), board.getName());
        }
//        else if (packet instanceof TeamS2CPacket pkt) {
//            if (pkt.getTeamOperation() == TeamS2CPacket.Operation.ADD) {
//                // CREATE
//                Team team = new Team(pkt.getTeamName());
//                team.setDisplayName(pkt.getTeamName());
//                team.setPrefix(pkt.getPrefix());
//                team.setSuffix(pkt.getSuffix());
//                team.setNameTagVisibility(NameTagVisibility.of(pkt.getNameTagVisibility()));
//                team.setColor(EnumChatFormatting.func_175744_a(pkt.getColor()));
//
//                for (String player : pkt.getPlayers()) {
//                    team.addTeamMember(player);
//                }
//
//                TeamManager.INSTANCE.createTeam(team);
//            } else if (pkt.getTeamOperation() == TeamS2CPacket.Operation.REMOVE) {
//                // REMOVE
//                TeamManager.INSTANCE.removeTeam(pkt.getName());
//            } else if (pkt.getPlayerListOperation() == TeamS2CPacket.Operation.ADD) {
//                // UPDATE
//                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
//                if (team != null) {
//                    for (String player : pkt.getPlayers()) {
//                        team.addTeamMember(player);
//                    }
//                }
//            } else if (pkt.getPlayerListOperation() == TeamS2CPacket.Operation.REMOVE) {
//                // PLAYER UPDATE
//                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
//                if (team != null) {
//                    for (String player : pkt.getPlayers()) {
//                        team.removeTeamMember(player);
//                    }
//                }
//            } else if (pkt.getTeamOperation() == null) {
//                // UOADTE
//                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
//                if (team != null) {
//                    team.setDisplayName(pkt.getDisplayName());
//                    team.setPrefix(pkt.getPrefix());
//                    team.setSuffix(pkt.getSuffix());
//                    team.setNameTagVisibility(NameTagVisibility.of(pkt.getNameTagVisibility()));
//                    team.setColor(EnumChatFormatting.func_175744_a(pkt.getColor()));
//                }
//            }
//        }
    }
//    public Packet onPacketReceive(Packet packet) { // this runs async.
//        return packet;
//    }
//
//    public void packetProcessPost(Packet packet) {  // this runs in sync.
//        if (packet instanceof S30PacketWindowItems) {
//            ItemStack[] stacks = ((S30PacketWindowItems) packet).getItemStacks();
//            List<WindowUpdateEvent.SlotUpdate> updates = new ArrayList<>();
//            for (int i = 0; i < stacks.length; i++) {
//                updates.add(new WindowUpdateEvent.SlotUpdate(i, stacks[i] == null ? null : new UItemStackImpl(stacks[i])));
//            }
//            ModAPI.getAPI().getEventBus().fireEvent(new WindowUpdateEvent(((S30PacketWindowItems) packet).func_148911_c(), updates, false));
//        } else if (packet instanceof S2FPacketSetSlot) {
//            ItemStack stack = ((S2FPacketSetSlot) packet).func_149174_e();
//            ModAPI.getAPI().getEventBus().fireEvent(new WindowUpdateEvent(((S2FPacketSetSlot) packet).func_149175_c(),
//                    Collections.singletonList(new WindowUpdateEvent.SlotUpdate(((S2FPacketSetSlot) packet).func_149173_d(), stack == null ? null : new UItemStackImpl(stack))), true));
//        } else if (packet instanceof S23PacketBlockChange) {
//            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
//            BlockPos blockPosition = ((S23PacketBlockChange) packet).getBlockPosition();
//            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
//            blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(
//                    new VectorI3D(
//                            blockPosition.getX(),
//                            blockPosition.getY(),
//                            blockPosition.getZ()
//                    ), impl.getByStateId(Block.BLOCK_STATE_IDS.get(((S23PacketBlockChange) packet).getBlockState()))));
//            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
//        } else if (packet instanceof S22PacketMultiBlockChange) {
//            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Post();
//            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
//            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : ((S22PacketMultiBlockChange) packet).getChangedBlocks()) {
//                blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(
//                        new VectorI3D(changedBlock.getPos().getX(), changedBlock.getPos().getY(), changedBlock.getPos().getZ())
//                        , impl.getByStateId(Block.BLOCK_STATE_IDS.get(changedBlock.getBlockState()))));
//            }
//            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
//        }else if (packet instanceof S21PacketChunkData) {
//            try {
//                if (((S21PacketChunkData) packet).getExtractedSize() == 0) return;
//                WorldProvider provider = new WorldProviderSurface();
//                if (Minecraft.getMinecraft().theWorld != null) provider = Minecraft.getMinecraft().theWorld.provider;
//                BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
//
//                Chunk c = new Chunk(new FakeWorld(provider), ((S21PacketChunkData) packet).getChunkX(), ((S21PacketChunkData) packet).getChunkZ());
//                c.fillChunk(((S21PacketChunkData) packet).getExtractedDataBytes(), ((S21PacketChunkData) packet).getExtractedSize(), ((S21PacketChunkData) packet).func_149274_i());
//                ChunkUpdateEvent.Post chunkUpdateEvent = new ChunkUpdateEvent.Post(Collections.singletonList(new UChunkImpl(c, impl)));
//                ModAPI.getAPI().getEventBus().fireEvent(chunkUpdateEvent);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else if (packet instanceof S26PacketMapChunkBulk) {
//            try {
//                WorldProvider provider = new WorldProviderSurface();
//                if (Minecraft.getMinecraft().theWorld != null) provider = Minecraft.getMinecraft().theWorld.provider;
//                BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
//
//                List<UChunk> set = new ArrayList<>();
//                for (int i = 0; i < ((S26PacketMapChunkBulk) packet).getChunkCount(); i++) {
//                    Chunk c = new Chunk(new FakeWorld(provider), ((S26PacketMapChunkBulk) packet).getChunkX(i), ((S26PacketMapChunkBulk) packet).getChunkZ(i));
//                    c.fillChunk(((S26PacketMapChunkBulk) packet).getChunkBytes(i), ((S26PacketMapChunkBulk) packet).getChunkSize(i), true);
//                    set.add(new UChunkImpl(c, impl));
//                }
//                ChunkUpdateEvent.Post chunkUpdateEvent = new ChunkUpdateEvent.Post(set);
//                ModAPI.getAPI().getEventBus().fireEvent(chunkUpdateEvent);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }else if (packet instanceof S34PacketMaps) { MapPacket
//            MapData mapData = MapDataManager.INSTANCE.createMapData(((S34PacketMaps) packet).getMapId());
//            try {
//                ((S34PacketMaps) packet).setMapdataTo(mapData);
//            } catch (Exception ignored) {} // hypixel seem to be sending bad map datas.
//            ModAPI.getAPI().getEventBus().fireEvent(new MapUpdateEvent(((S34PacketMaps) packet).getMapId(), new UMapDataImpl(mapData)));
//        } else if (packet instanceof S38PacketPlayerListItem) {
//            S38PacketPlayerListItem p = (S38PacketPlayerListItem) packet;
//            Map<UUID, NetworkPlayerInfo> playerInfoMap = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, Minecraft.getMinecraft().getNetHandler(), "playerInfoMap", "field_147310_i","i");
//            if (p.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
//                for (S38PacketPlayerListItem.AddPlayerData entry : p.getEntries()) {
//                    playerInfoMap.remove(entry.getProfile().getId());
//                    playerInfoMap.put(entry.getProfile().getId(), new CustomNetworkPlayerInfo(entry));
//                }
//            }
//        } else if (packet instanceof S45PacketTitle) {
//            S45PacketTitle title = (S45PacketTitle) packet;
//            TitleEvent titleEvent = new TitleEvent(
//                    title.getType() == S45PacketTitle.Type.TITLE ? TitleEvent.Type.TITLE :
//                            title.getType() == S45PacketTitle.Type.SUBTITLE ? TitleEvent.Type.SUBTITLE :
//                                    title.getType() == S45PacketTitle.Type.RESET ? TitleEvent.Type.RESET :
//                                            title.getType() == S45PacketTitle.Type.CLEAR ? TitleEvent.Type.CLEAR :
//                                                    TitleEvent.Type.TIMES,
//                    title.getMessage() == null ? null : GsonComponentSerializer.colorDownsamplingGson().deserialize(IChatComponent.Serializer.componentToJson(title.getMessage())),
//                    title.getFadeInTime(),
//                    title.getDisplayTime(),
//                    title.getFadeOutTime()
//            );
//            ModAPI.getAPI().getEventBus().fireEvent(titleEvent);
//        }
//    }
//
//    public void onPrePacketProcess(Packet packet) { // this runs in sync.
//        if (packet instanceof S23PacketBlockChange) {
//            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
//            BlockPos blockPosition = ((S23PacketBlockChange) packet).getBlockPosition();
//            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
//            blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(
//                    new VectorI3D(
//                            blockPosition.getX(),
//                            blockPosition.getY(),
//                            blockPosition.getZ()
//                    ), impl.getByStateId(Block.BLOCK_STATE_IDS.get(((S23PacketBlockChange) packet).getBlockState()))));
//            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
//        } else if (packet instanceof S22PacketMultiBlockChange) {
//            BlockUpdateEvent blockUpdateEvent = new BlockUpdateEvent.Pre();
//            BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
//            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : ((S22PacketMultiBlockChange) packet).getChangedBlocks()) {
//                blockUpdateEvent.getUpdatedBlocks().add(new Pair<>(new VectorI3D(
//                        changedBlock.getPos().getX(),
//                        changedBlock.getPos().getY(),
//                        changedBlock.getPos().getZ()
//                ), impl.getByStateId(Block.BLOCK_STATE_IDS.get(changedBlock.getBlockState()))));
//            }
//            ModAPI.getAPI().getEventBus().fireEvent(blockUpdateEvent);
//        } else if (packet instanceof S0DPacketCollectItem) {
//            UEntity possiblyItem = ModAPI.getAPI().getWorld().getEntityById(((S0DPacketCollectItem) packet).getCollectedItemEntityID());
//            UEntity possiblyPlayer = ModAPI.getAPI().getWorld().getEntityById(((S0DPacketCollectItem) packet).getEntityID());
//            if (possiblyItem instanceof UEntityItem) {
//                ModAPI.getAPI().getEventBus().fireEvent(new ItemPickupEvent(
//                        possiblyItem,
//                        possiblyPlayer
//                ));
//            }
//        }else if (packet instanceof S21PacketChunkData) {
//            try {
//                if (((S21PacketChunkData) packet).getExtractedSize() == 0) return;
//                WorldProvider provider = new WorldProviderSurface();
//                if (Minecraft.getMinecraft().theWorld != null) provider = Minecraft.getMinecraft().theWorld.provider;
//                BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
//
//                Chunk c = new Chunk(new FakeWorld(provider), ((S21PacketChunkData) packet).getChunkX(), ((S21PacketChunkData) packet).getChunkZ());
//                c.fillChunk(((S21PacketChunkData) packet).getExtractedDataBytes(), ((S21PacketChunkData) packet).getExtractedSize(), ((S21PacketChunkData) packet).func_149274_i());
//                ChunkUpdateEvent.Pre chunkUpdateEvent = new ChunkUpdateEvent.Pre(Collections.singletonList(new UChunkImpl(c, impl)));
//                ModAPI.getAPI().getEventBus().fireEvent(chunkUpdateEvent);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else if (packet instanceof S26PacketMapChunkBulk) {
//            try {
//                WorldProvider provider = new WorldProviderSurface();
//                if (Minecraft.getMinecraft().theWorld != null) provider = Minecraft.getMinecraft().theWorld.provider;
//                BlockStateRegistryImpl impl = (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry();
//
//                List<UChunk> set = new ArrayList<>();
//                for (int i = 0; i < ((S26PacketMapChunkBulk) packet).getChunkCount(); i++) {
//                    Chunk c = new Chunk(new FakeWorld(provider), ((S26PacketMapChunkBulk) packet).getChunkX(i), ((S26PacketMapChunkBulk) packet).getChunkZ(i));
//                    c.fillChunk(((S26PacketMapChunkBulk) packet).getChunkBytes(i), ((S26PacketMapChunkBulk) packet).getChunkSize(i), true);
//                    set.add(new UChunkImpl(c, impl));
//                }
//                ChunkUpdateEvent.Pre chunkUpdateEvent = new ChunkUpdateEvent.Pre(set);
//                ModAPI.getAPI().getEventBus().fireEvent(chunkUpdateEvent);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else if (packet instanceof S3BPacketScoreboardObjective) {
//            S3BPacketScoreboardObjective objectivePkt = (S3BPacketScoreboardObjective) packet;
//            if (objectivePkt.func_149338_e() == 2) {
//                Objective objective = ScoreboardManager.INSTANCE.getObjective(objectivePkt.func_149339_c());
//                if (objective != null) {
//                    objective.setDisplayName(objectivePkt.func_149337_d());
//                    objective.setDisplayType(objectivePkt.func_179817_d());
//                }
//            } else if (objectivePkt.func_149338_e() == 1) {
//                ScoreboardManager.INSTANCE.removeObjective(objectivePkt.func_149339_c());
//            } else if (objectivePkt.func_149338_e() == 0) {
//                Objective objective = new Objective(objectivePkt.func_149339_c());
//                objective.setDisplayName(objectivePkt.func_149337_d());
//                objective.setDisplayType(objectivePkt.func_179817_d());
//                ScoreboardManager.INSTANCE.addObjective(objective);
//            }
//        } else if (packet instanceof S3CPacketUpdateScore) {
//            S3CPacketUpdateScore score = (S3CPacketUpdateScore) packet;
//            Objective objective = ScoreboardManager.INSTANCE.getObjective(score.getObjectiveName());
//            if (objective != null) {
//                if (score.getScoreAction() == S3CPacketUpdateScore.Action.CHANGE) {
//                    objective.updateScore(score.getPlayerName(), score.getScoreValue());
//                } else if (score.getScoreAction() == S3CPacketUpdateScore.Action.REMOVE) {
//                    objective.removeScore(score.getPlayerName());
//                }
//            }
//        } else if (packet instanceof S3DPacketDisplayScoreboard) {
//            S3DPacketDisplayScoreboard board = (S3DPacketDisplayScoreboard) packet;
//            ScoreboardManager.INSTANCE.displayScoreboard(board.func_149371_c(), board.func_149370_d());
//        } else if (packet instanceof S3EPacketTeams) {
//            S3EPacketTeams pkt = (S3EPacketTeams) packet;
//            if (pkt.getAction() == 0) {
//                // CREATE
//                Team team = new Team(pkt.getName());
//                team.setDisplayName(pkt.getDisplayName());
//                team.setPrefix(pkt.getPrefix());
//                team.setSuffix(pkt.getSuffix());
//                team.setNameTagVisibility(NameTagVisibility.of(pkt.getNameTagVisibility()));
//                team.setColor(EnumChatFormatting.func_175744_a(pkt.getColor()));
//
//                for (String player : pkt.getPlayers()) {
//                    team.addTeamMember(player);
//                }
//
//                TeamManager.INSTANCE.createTeam(team);
//            } else if (pkt.getAction() == 1) {
//                // REMOVE
//                TeamManager.INSTANCE.removeTeam(pkt.getName());
//            } else if (pkt.getAction() == 2) {
//                // UPDATE
//                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
//                if (team != null) {
//                    team.setDisplayName(pkt.getDisplayName());
//                    team.setPrefix(pkt.getPrefix());
//                    team.setSuffix(pkt.getSuffix());
//                    team.setNameTagVisibility(NameTagVisibility.of(pkt.getNameTagVisibility()));
//                    team.setColor(EnumChatFormatting.func_175744_a(pkt.getColor()));
//                }
//            } else if (pkt.getAction() == 3) {
//                // PLAYER UPDATE
//                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
//                if (team != null) {
//                    for (String player : pkt.getPlayers()) {
//                        team.addTeamMember(player);
//                    }
//                }
//            } else if (pkt.getAction() == 4) {
//                // PLAYER REMOVE
//                Team team = TeamManager.INSTANCE.getTeamByName(pkt.getName());
//                if (team != null) {
//                    for (String player : pkt.getPlayers()) {
//                        team.removeTeamMember(player);
//                    }
//                }
//            }
//        } else if (packet instanceof S38PacketPlayerListItem) {
//            S38PacketPlayerListItem pkt = (S38PacketPlayerListItem) packet;
//            S38PacketPlayerListItem.Action action = pkt.getAction();
//            if (action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
//                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
//                    GameMode gameMode;
//                    switch (entry.getGameMode()) {
//                        case CREATIVE:
//                            gameMode = GameMode.CREATIVE;
//                        case SPECTATOR:
//                            gameMode = GameMode.SPECTATOR;
//                        case SURVIVAL:
//                            gameMode =  GameMode.SURVIVAL;
//                        case ADVENTURE:
//                            gameMode =  GameMode.ADVENTURE;
//                        default:
//                            gameMode = null;
//                    }
//
//                    TabListEntry tabListEntry = new TabListEntry(entry.getProfile(),gameMode);
//                    tabListEntry.setPing(entry.getPing());
//                    tabListEntry.setDisplayName(entry.getDisplayName());
//                    TabList.INSTANCE.updateEntry(tabListEntry);
//                }
//            } else if (action == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
//                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
//                    TabList.INSTANCE.removeEntry(entry.getProfile().getId());
//                }
//            } else if (action == S38PacketPlayerListItem.Action.UPDATE_LATENCY) {
//                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
//                    TabListEntry entry1 = TabList.INSTANCE.getEntry(entry.getProfile().getId());
//                    if (entry1 != null) entry1.setPing(entry.getPing());
//                }
//            } else if (action == S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME) {
//                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
//                    TabListEntry entry1 = TabList.INSTANCE.getEntry(entry.getProfile().getId());
//                    if (entry1 != null) entry1.setDisplayName(entry.getDisplayName());
//                }
//            } else if (action == S38PacketPlayerListItem.Action.UPDATE_GAME_MODE) {
//                for (S38PacketPlayerListItem.AddPlayerData entry : pkt.getEntries()) {
//                    TabListEntry entry1 = TabList.INSTANCE.getEntry(entry.getProfile().getId());
//                    if (entry1 == null) return;
//                    GameMode gameMode;
//                    switch (entry.getGameMode()) {
//                        case CREATIVE:
//                            gameMode = GameMode.CREATIVE;
//                        case SPECTATOR:
//                            gameMode = GameMode.SPECTATOR;
//                        case SURVIVAL:
//                            gameMode =  GameMode.SURVIVAL;
//                        case ADVENTURE:
//                            gameMode =  GameMode.ADVENTURE;
//                        default:
//                            gameMode = null;
//                    }
//                    TabListEntry neu = new TabListEntry(entry1.getGameProfile(), gameMode);
//                    neu.setPing(entry1.getPing());
//                    neu.setDisplayName(entry1.getDisplayName());
//
//                    TabList.INSTANCE.updateEntry(neu);
//                }
//            }
//
//
//            if (action == S38PacketPlayerListItem.Action.ADD_PLAYER || action == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
//                List<TabListEntry> entries = new ArrayList<>();
//                for (S38PacketPlayerListItem.AddPlayerData entry : ((S38PacketPlayerListItem) packet).getEntries()) {
//                    GameMode gameMode;
//                    switch (entry.getGameMode()) {
//                        case CREATIVE:
//                            gameMode = GameMode.CREATIVE;
//                        case SPECTATOR:
//                            gameMode = GameMode.SPECTATOR;
//                        case SURVIVAL:
//                            gameMode =  GameMode.SURVIVAL;
//                        case ADVENTURE:
//                            gameMode =  GameMode.ADVENTURE;
//                        default:
//                            gameMode = null;
//                    }
//                    entries.add(new TabListEntry(entry.getProfile(), gameMode));
//                }
//
//
//                TabListUpdateEvent updateEvent = new TabListUpdateEvent(
//                        action == S38PacketPlayerListItem.Action.ADD_PLAYER ? TabListUpdateEvent.Action.ADD_PLAYER : TabListUpdateEvent.Action.REMOVE_PLAYER,
//                        entries
//                );
//
//                ModAPI.getAPI().getEventBus().fireEvent(updateEvent);
//            }
//        }
//    }

}
