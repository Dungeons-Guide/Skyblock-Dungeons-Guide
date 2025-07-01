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

package kr.syeyoung.dungeonsguide.mod.commands;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatRoutine;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.MainConfigWidget;
import kr.syeyoung.dungeonsguide.mod.config.onboarding.OnboardingPage;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedMoveNearest;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.DungeonEventHolder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDungeonRooms;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay.RoomRouteHandler;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.gui.view.TestView;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Score;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.command.UCommandContext;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.world.BlockType;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CommandDgDebug {
    @DGCommand("dgdebug")
    public void showHelp() {
        ChatTransmitter.addToQueue("ain't gonna find much anything here");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg loadrooms §7-§f Reloads dungeon roomdata.");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg brand §7-§f View server brand.");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg info §7-§f View Current DG User info.");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg saverun §7-§f Save run to be sent to developer.");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg saverooms §7-§f Saves usergenerated dungeon roomdata.");
    }

    @DGCommand("dgdebug reloadshader")
    public void reloadshader() {
        ShaderManager.onResourceReload();
    }

    @DGCommand("dgdebug re")
    public void openRoomedit() {
        MainConfigWidget mainConfigWidget = new MainConfigWidget();
        GuiScreenAdapter adapter = new GuiScreenAdapter(new GlobalHUDScale(
                FeatureRegistry.ADVANCED_ROOMEDIT.getConfigureWidget()
        ));

        DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
            Minecraft.getMinecraft().displayGuiScreen(adapter);
        });
    }

    @DGCommand("dgdebug calculatestonks")
    public void calculateStonks() {
        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
            for (Map.Entry<String, DungeonMechanicData> stringDungeonMechanicEntry : dungeonRoomInfo.getMechanics().entrySet()) {
                DungeonMechanicData mechanic = stringDungeonMechanicEntry.getValue();
                if (mechanic instanceof DungeonFakeChestTrapState.DungeonFakeChestTrapData) {
                    DungeonFakeChestTrapState.DungeonFakeChestTrapData mechanic1 = (DungeonFakeChestTrapState.DungeonFakeChestTrapData) mechanic;
                    mechanic1.setChestCache(PrecalculatedStonk.createOne(
                            dungeonRoomInfo, mechanic1.getChest()
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getChestCache().getDependentRouteBlocker());
                    System.out.println(mechanic1.getChestCache().getPrecalculatedStonk(Collections.emptyList()).size());
                } else if (mechanic instanceof DungeonLeverState.DungeonLeverData) {
                    DungeonLeverState.DungeonLeverData mechanic1 = (DungeonLeverState.DungeonLeverData) mechanic;
                    mechanic1.setLeverCache(PrecalculatedStonk.createOne(
                            dungeonRoomInfo, mechanic1.getLeverPoint()
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getLeverCache().getDependentRouteBlocker());
                    System.out.println(mechanic1.getLeverCache().getPrecalculatedStonk(Collections.emptyList()).size());
                } else if (mechanic instanceof DungeonOnewayLeverState.DungeonOnewayLeverData) {
                    DungeonOnewayLeverState.DungeonOnewayLeverData mechanic1 = (DungeonOnewayLeverState.DungeonOnewayLeverData) mechanic;
                    mechanic1.setLeverCache(PrecalculatedStonk.createOne(
                            dungeonRoomInfo, mechanic1.getLeverPoint()
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getLeverCache().getDependentRouteBlocker());
                    System.out.println(mechanic1.getLeverCache().getPrecalculatedStonk(Collections.emptyList()).size());
                } else if (mechanic instanceof DungeonSecretChestState.DungeonSecretChestData) {
                    DungeonSecretChestState.DungeonSecretChestData mechanic1 = (DungeonSecretChestState.DungeonSecretChestData) mechanic;
                    mechanic1.setSecretCache(PrecalculatedStonk.createOne(
                            dungeonRoomInfo, mechanic1.getSecretPoint()
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getSecretCache().getDependentRouteBlocker());
                    System.out.println(mechanic1.getSecretCache().getPrecalculatedStonk(Collections.emptyList()).size());
                } else if (mechanic instanceof DungeonSecretEssenceState.DungeonSecretEssenceData) {
                    DungeonSecretEssenceState.DungeonSecretEssenceData mechanic1 = (DungeonSecretEssenceState.DungeonSecretEssenceData) mechanic;
                    mechanic1.setSecretCache(PrecalculatedStonk.createOne(
                            dungeonRoomInfo, mechanic1.getSecretPoint()
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getSecretCache().getDependentRouteBlocker());
                    System.out.println(mechanic1.getSecretCache().getPrecalculatedStonk(Collections.emptyList()).size());
                } else if (mechanic instanceof DungeonWizardCrystalState.DungeonWizardCrystalData) {
                    DungeonWizardCrystalState.DungeonWizardCrystalData mechanic1 = (DungeonWizardCrystalState.DungeonWizardCrystalData) mechanic;
                    mechanic1.setSecretCache(PrecalculatedStonk.createOne(
                            dungeonRoomInfo, mechanic1.getSecretPoint()
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getSecretCache().getDependentRouteBlocker());
                    System.out.println(mechanic1.getSecretCache().getPrecalculatedStonk(Collections.emptyList()).size());
                } else if (mechanic instanceof DungeonRedstoneKeyState.DungeonRedstoneKeyData) {
                    DungeonRedstoneKeyState.DungeonRedstoneKeyData mechanic1 = (DungeonRedstoneKeyState.DungeonRedstoneKeyData) mechanic;
                    mechanic1.setSecretCache(PrecalculatedStonk.createOne(
                            dungeonRoomInfo, mechanic1.getSecretPoint()
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getSecretCache().getDependentRouteBlocker());
                    System.out.println(mechanic1.getSecretCache().getPrecalculatedStonk(Collections.emptyList()).size());
                } else if (mechanic instanceof DungeonSecretDoubleChestState.DungeonSecretDoubleChestData) {
                    DungeonSecretDoubleChestState.DungeonSecretDoubleChestData mechanic1 = (DungeonSecretDoubleChestState.DungeonSecretDoubleChestData) mechanic;
                    mechanic1.setSecretCache(PrecalculatedStonk.createOne(
                            dungeonRoomInfo, mechanic1.getSecretPoint(), mechanic1.getSecretPoint2()
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getSecretCache().getDependentRouteBlocker());
                    System.out.println(mechanic1.getSecretCache().getPrecalculatedStonk(Collections.emptyList()).size());
                }

            }
        }

    }

    @DGCommand("dgdebug calculatenearests")
    public void calculateNearests() {
        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
            for (Map.Entry<String, DungeonMechanicData> stringDungeonMechanicEntry : dungeonRoomInfo.getMechanics().entrySet()) {
                DungeonMechanicData mechanic = stringDungeonMechanicEntry.getValue();
                if (mechanic instanceof DungeonSecretBatState.DungeonSecretBatData) {
                    DungeonSecretBatState.DungeonSecretBatData mechanic1 = (DungeonSecretBatState.DungeonSecretBatData) mechanic;
                    mechanic1.setMoveNearest(PrecalculatedMoveNearest.createOneBat(
                            mechanic1.getSecretPoint(),
                            dungeonRoomInfo
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getMoveNearest().getDependentRouteBlocker());
                    System.out.println(mechanic1.getMoveNearest().getPrecalculatedStonk(Collections.emptyList()).size());
                } else if (mechanic instanceof DungeonSecretItemDropState.DungeonSecretItemDropData) {
                    DungeonSecretItemDropState.DungeonSecretItemDropData mechanic1 = (DungeonSecretItemDropState.DungeonSecretItemDropData) mechanic;
                    mechanic1.setMoveNearest(PrecalculatedMoveNearest.createOneItem(
                            mechanic1.getSecretPoint(),
                            dungeonRoomInfo
                    ));
                    System.out.println(dungeonRoomInfo.getName()+"/"+stringDungeonMechanicEntry.getKey()+"/"+ mechanic1.getMoveNearest().getDependentRouteBlocker());
                    System.out.println(mechanic1.getMoveNearest().getPrecalculatedStonk(Collections.emptyList()).size());
                }
            }
        }
    }



    @DGCommand("dgdebug freeze")
    public void freeze() {
        while(true);
    }

    @DGCommand("dgdebug scoreboard")
    public void scoreboardCommand() {
        for (Score score : ScoreboardManager.INSTANCE.getSidebarObjective().getScores()) {
            ChatTransmitter.addToQueue("LINE: " + score.getVisibleName() + ": " + score.getScore());
        }
    }


    @DGCommand("dgdebug scoreboardclean")
    public void scoreboardCleanCommand() {
        for (Score score : ScoreboardManager.INSTANCE.getSidebarObjective().getScores()) {
            ChatTransmitter.addToQueue("LINE: " + score.getJustTeam() + ": " + score.getScore());
        }
    }

    @DGCommand("dgdebug tablist")
    public void tabListCommand() {
        for (TabListEntry entry : TabList.INSTANCE.getTabListEntries()) {
            ChatTransmitter.addToQueue(entry.getFormatted() + " " + entry.getEffectiveName() + "(" + entry.getPing() + ")" + entry.getGameMode());
        }
        ChatTransmitter.addToQueue("VS");
    }


    @DGCommand("dgdebug mockdungeonstart {time}")
    public void mockDungeonStartCommand(int time) {
        if (!ModAPI.getAPI().isSinglePlayer()) {
            ChatTransmitter.addToQueue("This only works in singlepauer", false);
            return;
        }

        if (time != 0) {
            ChatTransmitter.addToQueue("§r§aDungeon starts in " + time + " seconds.§r", false);
            return;
        }


        (new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            try {
                ChatTransmitter.addToQueue("§r§aDungeon starts in 15 seconds.§r", false);
                Thread.sleep(6000);
                ChatTransmitter.addToQueue("§r§aDungeon starts in 10 seconds.§r", false);
                Thread.sleep(700);
                ChatTransmitter.addToQueue("§r§aDungeon starts in 5 seconds.§r", false);
                Thread.sleep(1000);
                ChatTransmitter.addToQueue("§r§aDungeon starts in 4 seconds.§r", false);
                Thread.sleep(1000);
                ChatTransmitter.addToQueue("§r§aDungeon starts in 3 seconds.§r", false);
                Thread.sleep(1000);
                ChatTransmitter.addToQueue("§r§aDungeon starts in 2 seconds.§r", false);
                Thread.sleep(1000);
                ChatTransmitter.addToQueue("§r§aDungeon starts in 1 seconds.§r", false);
            } catch (InterruptedException ignored) {
            }
        })).start();
    }

    @DGCommand("dgdebug saverooms")
    public void saveRoomsCommand() {
        DungeonRoomInfoRegistry.saveAll(new File(Main.getConfigDir(), "roomdatas"));
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fSuccessfully saved user generated roomdata");
    }

    @DGCommand("dgdebug process2")
    public void process2() throws IOException {

        GuiScreenAdapter adapter = new GuiScreenAdapter(new GlobalHUDScale(new OnboardingPage("pages/front.gui")), null, false);
        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
                Minecraft.getMinecraft().displayGuiScreen(adapter);
            });
        }).start();
        String features = "advanced.coords,dungeon.map2,secret.actionview,bossfight.health,bossfight.spiritbear,bossfight.spiritbowdisplay,bossfight.terracota,bossfight.phasedisplay,party.list,party.readylist,secret.fairysoulwarn,dungen.watcherwarn,dungeon.lowhealthwarn,dungeon.stats.score,dungeon.stats.tombs,dungeon.stats.totaltombs,dungeon.stats.secretsroom,dungeon.stats.secrets,dungeon.stats.igtime,dungeon.stats.realtime,dungeon.stats.milestone,dungeon.stats.deaths,dungeon.roomname,etc.abilitycd2,qol.cooldown";
        StringBuilder sb = new StringBuilder();
        Gson gson = new Gson();
        for (String s : features.split(",")) {
            sb.append("<feature id=\"").append(s).append("\">\n\t<_>");

            JsonObject jsonObject = FeatureRegistry.getFeatureByKey(s).saveConfig();
            jsonObject.remove("newstyle");
            sb.append(jsonObject.toString());
            sb.append("</_>\n</feature>\n");
        }

        System.out.println(sb.toString());

//        int cnt = 0;
//        CBORMapper objectMapper = new CBORMapper();
//        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
//            System.out.println(dungeonRoomInfo.getName());
//            byte[] str = objectMapper.writeValueAsBytes(dungeonRoomInfo);
//            DungeonRoomInfo info2 = objectMapper.readValue(str, DungeonRoomInfo.class);
//
//            System.out.println(dungeonRoomInfo.equals(info2) +" :: "+dungeonRoomInfo.getName());
//            cnt++;
//        }
//        System.out.println(cnt);


//        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
//            for (Map.Entry<String, DungeonMechanicData> stringDungeonMechanicDataEntry : dungeonRoomInfo.getMechanics().entrySet()) {
//                if (stringDungeonMechanicDataEntry.getValue() instanceof DungeonCrusherTrapState.DungeonCrusherTrapData) {
//                    OffsetPointSet crusherBlocks = ((DungeonCrusherTrapState.DungeonCrusherTrapData) stringDungeonMechanicDataEntry.getValue()).getStarting();
//                    IBlockState blockState = dungeonRoomInfo.getBlock(crusherBlocks.getOffsetPointList().get(0), 0);
//
//
//
//                    OffsetPointSet ops = ((DungeonCrusherTrapState.DungeonCrusherTrapData) stringDungeonMechanicDataEntry.getValue()).getDangerRegion();
//                    for (OffsetPoint offsetPoint : ops.getOffsetPointList()) {
//                        if (dungeonRoomInfo.getBlock(offsetPoint, 0) == blockState) {
//                            System.out.println("how: "+dungeonRoomInfo.getName());
//                            dungeonRoomInfo.setBlock(offsetPoint, Blocks.air.getDefaultState());
//                        }
//                    }
//                }
//            }
//        }

//        for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
//            System.out.println(abstractFeature.getCategory() +" - " +abstractFeature.getName()+"/ "+abstractFeature.getKey());
//        }
    }

    @DGCommand("dgdebug loadrooms")
    public void loadRoomsCommand() {
        try {
            DungeonRoomInfoRegistry.loadAll(new File(Main.getConfigDir(), "roomdatas"));
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fSuccessfully loaded roomdatas");
            return;
        } catch (BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException |
                 NoSuchAlgorithmException | IOException | IllegalBlockSizeException |
                 NoSuchPaddingException e) {
            e.printStackTrace();
        }
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cAn error has occurred while loading roomdata");
    }
    @DGCommand("dgdebug brand")
    public void brandCommand() {
        String serverBrand = ModAPI.getAPI().getPlayer().getClientBrand();
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e" + serverBrand);
    }

    @DGCommand("dgdebug removedoors")
    public void removedoors() throws Exception {
        File fileRoot = Main.getConfigDir();
        File dir = new File(fileRoot, "grouped2");
        File outdir = new File(fileRoot, "grouped3");


        Iterator<File> fileIter = FileUtils.iterateFiles(dir, new String[] {"dgrun"}, true);

        while (fileIter.hasNext()) {

            try {
                File f = fileIter.next();
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(IOUtils.toString(f.toURI()), JsonObject.class);


                CompoundBinaryTag compound = BinaryTagIO.reader(10_000_000).readNamed(new ByteArrayInputStream(Base64.getDecoder().decode(
                        jsonObject.get("schematic").getAsString()
                )), BinaryTagIO.Compression.GZIP).getValue();
                byte[] blocks = compound.getByteArray("Blocks");
                byte[] meta = compound.getByteArray("Data");
                int shape = jsonObject.get("shape").getAsShort();
                int len = compound.getShort("Length");
                int wid = compound.getShort("Width");
                // formula y *len*width + z * width + x

                for (int x = 0; x <= 11; x ++) {
                    for (int z = 0; z <= 11; z++) {
                        if ((x % 2 == 1) == (z % 2 == 1)) continue;

                        int rx = x * 16;
                        int rz = z * 16;

                        if (rx >= wid+6) continue;
                        if (rz >= len+6) continue;

                        if (x % 2 == 1) {
                            if (z == 0) {
                            } else if ((shape >>(((z/2)-1) *4 +(x/2)) & 0x1) > 0 &&
                                    (shape >>(((z/2)) *4 +(x/2)) & 0x1) > 0) {
                                continue;
                            }
                        } else {
                            if (x == 0) {
                            } else if ((shape >>((z/2) *4 +(x/2) - 1) & 0x1) > 0 &&
                                    (shape >>((z/2) *4 +(x/2)) & 0x1) > 0) {
                                continue;
                            }
                        }

                        int f1 = 13;
                        // paste the one with TERRACOTA or COAL or MORE AIR INSIDE
                        for (int rrx = rx-1; rrx <= rx + 1; rrx ++) {
                            for (int rrz = rz - 1; rrz <= rz +1; rrz ++) {
                                for (int y = 69; y <= 72; y++) {
                                    int i = rrx + rrz * wid + y * wid * len;
                                    if (rrx >= wid) continue;
                                    if (rrz >= len) continue;
                                    if (rrx < 0) continue;
                                    if (rrz < 0) continue;

                                    if (blocks[i] == 0 || blocks[i] == (byte)173 || (blocks[i] == (byte)159 && meta[i] == 14)) {
                                    } else {
                                        f1 = 0;
                                    }
                                }
                            }
                        }

                        if (f1 < 12) continue;

                        if (x % 2 == 1) {
                            // going in Z dir
                            for (int rrx = rx-2; rrx <= rx + 2; rrx ++) {
                                for (int rrz = rz - 3; rrz <= rz +3; rrz ++) {
                                    for (int y = 66; y <= 73; y++) {
                                        int i = rrx + rrz * wid + y * wid * len;
                                        if (rrx >= wid) continue;
                                        if (rrz >= len) continue;
                                        if (rrx < 0) continue;
                                        if (rrz < 0) continue;

                                        if (Math.abs(rrx - rx) == 2 || y >= 72 || y < 69) {
                                            blocks[i] = (byte) 153;
                                            meta[i] = 14;
                                        }

                                        if (Math.abs(rrz - rz) <= 1 && Math.abs(rrx - rx) <= 1 && (y >= 69 && y <= 72)) {
                                            blocks[i] = (byte) 19;
                                            meta[i] = 14;
                                        }
                                    }
                                }
                            }
                        } else {
                            // going in X dir
                            for (int rrx = rx-3; rrx <= rx + 3; rrx ++) {
                                for (int rrz = rz - 2; rrz <= rz +2; rrz ++) {
                                    for (int y = 66; y <= 73; y++) {
                                        int i = rrx + rrz * wid + y * wid * len;
                                        if (rrx >= wid) continue;
                                        if (rrz >= len) continue;
                                        if (rrx < 0) continue;
                                        if (rrz < 0) continue;

                                        if (Math.abs(rrz - rz) == 2 || y >= 72 || y < 69) {
                                            blocks[i] = (byte) 153;
                                            meta[i] = 15;
                                        }

                                        if (Math.abs(rrz - rz) <= 1 && Math.abs(rrx - rx) <= 1 && (y >= 69 && y <= 72)) {
                                            blocks[i] = (byte) 19;
                                            meta[i] = 15;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                compound = CompoundBinaryTag.builder()
                                .put(compound)
                                .putByteArray("Blocks", blocks)
                                .putByteArray("Data", meta).build();

                String schm = FeatureCollectDungeonRooms.nbttostring("Schematic", compound);
                jsonObject.remove("schematic");
                jsonObject.addProperty("schematic", schm);

                JsonWriter writer = new JsonWriter(new OutputStreamWriter(Files.newOutputStream(new File(outdir, f.getName()).toPath())));
                gson.toJson(jsonObject, writer);
                writer.flush();
                writer.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @DGCommand("dgdebug removedoorschematic {file}")
    public void removedoorsSchematic(@CommandParam(value = "file", stringType = CommandParam.EnumStringType.GREEDY) String file) throws Exception {
        File fileRoot = Main.getConfigDir();
        File dir = new File(fileRoot, "schematics");

        File outdir = new File(fileRoot, "schematics");


//        Iterator<File> fileIter = FileUtils.iterateFiles(dir, new String[] {"dgrun"}, true);

        File f = new File(dir, file);
            try (FileInputStream fis = new FileInputStream(f)){
                CompoundBinaryTag compound = BinaryTagIO.reader(10_000_000).readNamed(fis, BinaryTagIO.Compression.GZIP).getValue();
                byte[] blocks = compound.getByteArray("Blocks");
                byte[] meta = compound.getByteArray("Data");
                int shape = 1;
                int len = compound.getShort("Length");
                int wid = compound.getShort("Width");
                // formula y *len*width + z * width + x

                for (int x = 0; x <= 11; x ++) {
                    for (int z = 0; z <= 11; z++) {
                        if ((x % 2 == 1) == (z % 2 == 1)) continue;

                        int rx = x * 16;
                        int rz = z * 16;

                        if (rx >= wid+6) continue;
                        if (rz >= len+6) continue;

                        if (x % 2 == 1) {
                            if (z == 0) {
                            } else if ((shape >>(((z/2)-1) *4 +(x/2)) & 0x1) > 0 &&
                                    (shape >>(((z/2)) *4 +(x/2)) & 0x1) > 0) {
                                continue;
                            }
                        } else {
                            if (x == 0) {
                            } else if ((shape >>((z/2) *4 +(x/2) - 1) & 0x1) > 0 &&
                                    (shape >>((z/2) *4 +(x/2)) & 0x1) > 0) {
                                continue;
                            }
                        }

                        int f1 = 13;
                        // paste the one with TERRACOTA or COAL or MORE AIR INSIDE
                        for (int rrx = rx-1; rrx <= rx + 1; rrx ++) {
                            for (int rrz = rz - 1; rrz <= rz +1; rrz ++) {
                                for (int y = 69; y <= 72; y++) {
                                    int i = rrx + rrz * wid + y * wid * len;
                                    if (rrx >= wid) continue;
                                    if (rrz >= len) continue;
                                    if (rrx < 0) continue;
                                    if (rrz < 0) continue;

                                    if (blocks[i] == 0 || blocks[i] == (byte)173 || (blocks[i] == (byte)159 && meta[i] == 14)) {
                                    } else {
                                        f1 = 0;
                                    }
                                }
                            }
                        }

                        if (f1 < 12) continue;

                        if (x % 2 == 1) {
                            // going in Z dir
                            for (int rrx = rx-2; rrx <= rx + 2; rrx ++) {
                                for (int rrz = rz - 3; rrz <= rz +3; rrz ++) {
                                    for (int y = 66; y <= 73; y++) {
                                        int i = rrx + rrz * wid + y * wid * len;
                                        if (rrx >= wid) continue;
                                        if (rrz >= len) continue;
                                        if (rrx < 0) continue;
                                        if (rrz < 0) continue;

                                        if (Math.abs(rrx - rx) == 2 || y >= 72 || y < 69) {
                                            blocks[i] = (byte) 153;
                                            meta[i] = 14;
                                        }

                                        if (Math.abs(rrz - rz) <= 1 && Math.abs(rrx - rx) <= 1 && (y >= 69 && y <= 72)) {
                                            blocks[i] = (byte) 19;
                                            meta[i] = 14;
                                        }
                                    }
                                }
                            }
                        } else {
                            // going in X dir
                            for (int rrx = rx-3; rrx <= rx + 3; rrx ++) {
                                for (int rrz = rz - 2; rrz <= rz +2; rrz ++) {
                                    for (int y = 66; y <= 73; y++) {
                                        int i = rrx + rrz * wid + y * wid * len;
                                        if (rrx >= wid) continue;
                                        if (rrz >= len) continue;
                                        if (rrx < 0) continue;
                                        if (rrz < 0) continue;

                                        if (Math.abs(rrz - rz) == 2 || y >= 72 || y < 69) {
                                            blocks[i] = (byte) 153;
                                            meta[i] = 15;
                                        }

                                        if (Math.abs(rrz - rz) <= 1 && Math.abs(rrx - rx) <= 1 && (y >= 69 && y <= 72)) {
                                            blocks[i] = (byte) 19;
                                            meta[i] = 15;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                compound = CompoundBinaryTag.builder()
                        .put(compound)
                        .putByteArray("Blocks", blocks)
                        .putByteArray("Data", meta).build();


                OutputStream outputStream = Files.newOutputStream(new File(outdir, "fixed-"+f.getName()).toPath());

                BinaryTagIO.writer().writeNamed(new AbstractMap.SimpleEntry<>("Schematic", compound), outputStream, BinaryTagIO.Compression.GZIP);

                outputStream.flush();
                outputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

    }


    @DGCommand("dgdebug nodupeprocess")
    public void removedupe() throws Exception  {

        File fileRoot = Main.getConfigDir();
        File dir = new File(fileRoot, "grouped");
        File outdir = new File(fileRoot, "grouped2");

        Iterator<File> fileIter = FileUtils.iterateFiles(dir, new String[] {"dgrun"}, true);

        while (fileIter.hasNext()) {
            try {
                File f = fileIter.next();
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(IOUtils.toString(f.toURI()), JsonObject.class);


                CompoundBinaryTag compound = BinaryTagIO.reader(10_000_000).readNamed(new ByteArrayInputStream(Base64.getDecoder().decode(
                        jsonObject.get("schematic").getAsString()
                )), BinaryTagIO.Compression.GZIP).getValue();
                byte[] blocks = compound.getByteArray("Blocks");
                byte[] meta = compound.getByteArray("Data");

                JsonArray jsonElements = new JsonArray();
                Set<String> updated = new HashSet<>();
                for (JsonElement _blockupdates : jsonObject.getAsJsonArray("blockupdates")) {
                    JsonObject blockupdates = _blockupdates.getAsJsonObject();
                    JsonArray realUpdatedBlocks = new JsonArray();
                    for (JsonElement updatedBlocks : blockupdates.getAsJsonArray("updatedBlocks")) {
                        JsonArray blockData = updatedBlocks.getAsJsonArray();
                        int x = blockData.get(0).getAsInt() - jsonObject.get("minX").getAsInt();
                        int y = blockData.get(1).getAsInt();
                        int z = blockData.get(2).getAsInt() - jsonObject.get("minZ").getAsInt();
                        String block = blockData.get(3).getAsString().split(":")[0];

                        if (updated.contains(x+":"+y+":"+z+":"+block)) continue;
                        updated.add(x+":"+y+":"+z+":"+block);
                        realUpdatedBlocks.add(blockData);
                    }

                    if (realUpdatedBlocks.size() > 0) {
                        blockupdates.remove("updatedBlocks");
                        blockupdates.add("updatedBlocks", realUpdatedBlocks);
                        jsonElements.add(blockupdates);
                    }
                }

                jsonObject.remove("blockupdates");
                jsonObject.add("blockupdates", jsonElements);

                JsonWriter writer = new JsonWriter(new OutputStreamWriter(Files.newOutputStream(new File(outdir, f.getName()).toPath())));
                gson.toJson(jsonObject, writer);
                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @DGCommand("dgdebug groupunknowns")
    public void groupunknowns() throws Exception {

        File fileRoot = Main.getConfigDir();
        File dir = new File(fileRoot, "compressed");
        File outdir = new File(fileRoot, "unk_grouped");
        Iterator<File> fileIter = FileUtils.iterateFiles(dir, new String[] {"dgrun"}, true);

        while (fileIter.hasNext()) {
            try {
                File f = fileIter.next();
                if (!f.getParentFile().getName().startsWith("The")) continue;
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(IOUtils.toString(f.toURI()), JsonObject.class);
                if (jsonObject == null) continue;
                if (!jsonObject.get("uuid").getAsString().equalsIgnoreCase(jsonObject.get("name").getAsString())) {
                    continue;
                }
                System.out.println("Processing: " + f.getCanonicalPath());
                int color = jsonObject.get("color").getAsInt();
                int size = jsonObject.get("shape").getAsInt();
                if (size != 1) continue;
                if (color == 18) {
                    // red
                    File target = new File(outdir, "blood/"+jsonObject.get("dungeon").getAsString());
                    target.mkdirs();
                    Files.copy(f.toPath(), new File(target, f.getName()).toPath());
                } else if (color == 30) {
                    // ent
                    File target = new File(outdir, "entrance/"+jsonObject.get("dungeon").getAsString());
                    target.mkdirs();
                    Files.copy(f.toPath(), new File(target, f.getName()).toPath());
                } else if (color == 74) {
                    // miniboss
                    File target = new File(outdir, "miniboss/"+jsonObject.get("dungeon").getAsString());
                    target.mkdirs();
                    Files.copy(f.toPath(), new File(target, f.getName()).toPath());
                } else if (color == 63) {
                    // smth i haven't visited yeet
                    // check

                    CompoundBinaryTag compound = BinaryTagIO.reader(10_000_000).readNamed(new ByteArrayInputStream(Base64.getDecoder().decode(
                            jsonObject.get("schematic").getAsString()
                    )), BinaryTagIO.Compression.GZIP).getValue();
                    byte[] blocks = compound.getByteArray("Blocks");
                    byte[] meta = compound.getByteArray("Data");

                    int len = compound.getShort("Length");
                    int wid = compound.getShort("Width");

                    int air = 0;
                    for (int i = 1; i <= 31; i++) {
                        if (blocks[1 + i * wid + 70 * wid * len] == 0)
                            air++;
                        if (blocks[1 + i * wid + 70 * wid * len] == (byte)173)
                            air += 3;
                        if (blocks[31 + i * wid + 70 * wid * len] == 0)
                            air++;
                        if (blocks[31 + i * wid + 70 * wid * len] == (byte)173)
                            air += 3;
                        if (blocks[i + 1 * wid + 70 * wid * len] == 0)
                            air++;
                        if (blocks[i + 1 * wid + 70 * wid * len] == (byte)173)
                            air += 3;
                        if (blocks[i + 31 * wid + 70 * wid * len] == 0)
                            air++;
                        if (blocks[i + 31 * wid + 70 * wid * len] == (byte)173)
                            air += 3;
                    }
                    if (air == 3) {

                        File target = new File(outdir, "rare/"+jsonObject.get("dungeon").getAsString());
                        target.mkdirs();
                        Files.copy(f.toPath(), new File(target, f.getName()).toPath());
                    } else {

                        File target = new File(outdir, "uhhwaht/"+jsonObject.get("dungeon").getAsString());
                        target.mkdirs();
                        Files.copy(f.toPath(), new File(target, f.getName()).toPath());
                    }
                } else {
                    File target = new File(outdir, "uhhwaht/"+jsonObject.get("dungeon").getAsString());
                    target.mkdirs();
                    Files.copy(f.toPath(), new File(target, f.getName()).toPath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @DGCommand("dgdebug groupprocess")
    public void groupprocess() throws Exception {
        // This take about 7m to complete.  :30:35 to 39:19 -> Around 9min.
        File fileRoot = Main.getConfigDir();
        File dir = new File(fileRoot, "compressed");
        File outdir = new File(fileRoot, "grouped");
        Iterator<File> fileIter = FileUtils.iterateFiles(dir, new String[] {"dgrun"}, true);

        Map<String, JsonObject> roomMapping = new HashMap<>();

        while (fileIter.hasNext()) {
            try {
                File f = fileIter.next();
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(IOUtils.toString(f.toURI()), JsonObject.class);
                if (jsonObject == null) continue;
                System.out.println("Processing: " + f.getCanonicalPath());
                if (jsonObject.get("uuid").getAsString().equalsIgnoreCase(jsonObject.get("name").getAsString())) {
                    continue;
                }


                CompoundBinaryTag compound = BinaryTagIO.reader(10_000_000).readNamed(new ByteArrayInputStream(Base64.getDecoder().decode(
                        jsonObject.get("schematic").getAsString()
                )), BinaryTagIO.Compression.GZIP).getValue();
                byte[] blocks = compound.getByteArray("Blocks");
                byte[] meta = compound.getByteArray("Data");
                // to get

                JsonArray jsonElements = new JsonArray();
                Set<String> updated = new HashSet<>();
                boolean chestpopulated = false;
                for (JsonElement _blockupdates : jsonObject.getAsJsonArray("blockupdates")) {
                    JsonObject blockupdates = _blockupdates.getAsJsonObject();
                    JsonArray realUpdatedBlocks = new JsonArray();
                    for (JsonElement updatedBlocks : blockupdates.getAsJsonArray("updatedBlocks")) {
                        JsonArray blockData = updatedBlocks.getAsJsonArray();
                        int x = blockData.get(0).getAsInt() - jsonObject.get("minX").getAsInt();
                        int y = blockData.get(1).getAsInt();
                        int z = blockData.get(2).getAsInt() - jsonObject.get("minZ").getAsInt();
                        String block = blockData.get(3).getAsString().split(":")[0];

                        int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
                        if (index >= blocks.length) continue;
                        if (index < 0) continue;
                        String worldBlock = (blocks[index] & 0xFF)+"";
                        if (block.equals(worldBlock)) continue;
                        if (updated.contains(x+":"+y+":"+z+":"+block)) continue;
                        if (block.equals("0") && blockupdates.getAsJsonArray("updatedBlocks").size() < 3) continue;
                        if (block.equals("0") && blockupdates.getAsJsonArray("updatedBlocks").size() > 800) continue;
                        if (block.equals("148")) continue; // trap plate
                        if (x == 0 || z == 0 && (y < 66 || y > 73)) continue;
                        if (x >=  compound.getShort("Width") || z >=  compound.getShort("Length")) continue;
                        if (block.equals("45")) continue; // bricks... mages do be like..
                        if (block.equals("79")) continue; // ice... apparently some1 uses frostworker
                        updated.add(x+":"+y+":"+z+":"+block);
                        realUpdatedBlocks.add(blockData);
                        // redstone bloc / chest  / lever / tripwire / tripwire hook / button / trapped chest
                        if (block.equals("152") || block.equals("54") || block.equals("69") || block.equals("132") || block.equals("131") || block.equals("77") || block.equals("146")) {
                            blocks[index] = (byte) Integer.parseInt(block);
                            meta[index] = (byte) Integer.parseInt(blockData.get(3).getAsString().split(":")[1]);
                            chestpopulated = true;
                        }
                    }
                    if (realUpdatedBlocks.size() > 0) {
                        blockupdates.remove("updatedBlocks");
                        blockupdates.add("updatedBlocks", realUpdatedBlocks);
                        jsonElements.add(blockupdates);
                    }
                }
                jsonObject.remove("blockupdates");
                jsonObject.add("blockupdates", jsonElements);


                if (chestpopulated) {
                    compound = CompoundBinaryTag.builder()
                            .put(compound)
                            .putByteArray("Blocks", blocks)
                            .putByteArray("Data", meta).build();

                    String schm = FeatureCollectDungeonRooms.nbttostring("Schematic", compound);
                    jsonObject.remove("schematic");
                    jsonObject.addProperty("schematic", schm);
                }

                DungeonRoomInfo dungeonRoomInfo = DungeonRoomInfoRegistry.getByUUID(UUID.fromString(jsonObject.get("uuid").getAsString()));

                if (!roomMapping.containsKey(jsonObject.get("uuid").getAsString())) {
                    roomMapping.put(jsonObject.get("uuid").getAsString(), jsonObject);
                } else {
                    // MERGE BLOCK UPDATES!
                    JsonObject originalRoomMapping = roomMapping.get(jsonObject.get("uuid").getAsString());
                    int originalRot = originalRoomMapping.get("rot").getAsInt();
                    JsonArray toMergeInto = originalRoomMapping.getAsJsonArray("blockupdates");
                    int thisRot = jsonObject.get("rot").getAsInt();


                    CompoundBinaryTag compound2 = BinaryTagIO.reader(10_000_000).readNamed(new ByteArrayInputStream(Base64.getDecoder().decode(
                            originalRoomMapping.get("schematic").getAsString()
                    )), BinaryTagIO.Compression.GZIP).getValue();
                    byte[] blocks2 = compound2.getByteArray("Blocks");
                    byte[] meta2 = compound2.getByteArray("Data");
                    int len = compound.getShort("Length");
                    int wid = compound.getShort("Width");

                    boolean changed = false;

                    for (JsonElement _blockUpdates : jsonElements) {
                        JsonObject blockUpdate = _blockUpdates.getAsJsonObject();
                        JsonArray transformedUpdatedBlocks = new JsonArray();
                        for (JsonElement updatedBlocks : blockUpdate.getAsJsonArray("updatedBlocks")) {
                            JsonArray blockData = updatedBlocks.getAsJsonArray();

                            int x = blockData.get(0).getAsInt();
                            int y = blockData.get(1).getAsInt();
                            int z = blockData.get(2).getAsInt();
                            String block = blockData.get(3).getAsString();

                            x -= jsonObject.get("minX").getAsInt();
                            z -= jsonObject.get("minZ").getAsInt();
                            for (int i = 0; i < (thisRot - originalRot) + 4; i++) {
                                int tempX = x;
                                x = -z;
                                z = tempX;
                                if (i % 2 == 0) {
                                    x += dungeonRoomInfo.getBlocks()[0].length - 1; // + Z
                                } else {
                                    x += dungeonRoomInfo.getBlocks().length - 1; // + X
                                }
                            }
                            x += originalRoomMapping.get("minX").getAsInt();
                            z += originalRoomMapping.get("minZ").getAsInt();
                            JsonArray brueuru = new JsonArray();
                            brueuru.add(new JsonPrimitive(x));
                            brueuru.add(new JsonPrimitive(y));
                            brueuru.add(new JsonPrimitive(z));
                            brueuru.add(new JsonPrimitive(block));

                            transformedUpdatedBlocks.add(brueuru);

                            if (block.startsWith("152:") || block.startsWith("54:") || block.startsWith("69:") || block.startsWith("132:") || block.startsWith("131:") || block.startsWith("77:") || block.startsWith("146:")) {
                                int i = x - originalRoomMapping.get("minX").getAsInt() + (z - originalRoomMapping.get("minZ").getAsInt()) * wid + y * wid * len;
                                if (x - originalRoomMapping.get("minX").getAsInt() < 0 || z - originalRoomMapping.get("minZ").getAsInt() < 0 || y < 0 || x - originalRoomMapping.get("minX").getAsInt() >= wid || z - originalRoomMapping.get("minZ").getAsInt() >= len || y >= 256) continue;

                                blocks2[i] = (byte) Integer.parseInt(block.split(":")[0]);
                                meta2[i] = (byte) Integer.parseInt(block.split(":")[1]);
                                changed = true;
                            }
                        }
                        blockUpdate.remove("updatedBlocks");
                        blockUpdate.add("updatedBlocks", transformedUpdatedBlocks);
                        toMergeInto.add(blockUpdate);
                    }

                    // PREFER MOAR BLOCKS!!

                    if (originalRot == thisRot) {

                        // formula y *len*width + z * width + x

                        for (int x = 0; x <= 11; x ++) {
                            for (int z = 0; z <= 11; z++) {
                                if ((x % 2 == 1) == (z % 2 == 1)) continue;

                                int rx = x * 16;
                                int rz = z * 16;

                                if (rx >= wid+6) continue;
                                if (rz >= len+6) continue;

                                int f1 = 0, s1 = 0, mismatch = 0;
                                // paste the one with TERRACOTA or COAL or MORE AIR INSIDE
                                for (int rrx = rx-1; rrx <= rx + 1; rrx ++) {
                                    for (int rrz = rz - 1; rrz <= rz +1; rrz ++) {
                                        for (int y = 69; y <= 72; y++) {
                                            int i = rrx + rrz * wid + y * wid * len;
                                            if (rrx >= wid) continue;
                                            if (rrz >= len) continue;
                                            if (rrx < 0) continue;
                                            if (rrz < 0) continue;

                                            if (blocks[i] == 0 || blocks[i] == (byte)173 || (blocks[i] == (byte)159 && meta[i] == 14)) {
                                                f1 ++;
                                            }
                                            if (blocks2[i] == 0 || blocks2[i] == (byte)173 || (blocks2[i] == (byte)159 && meta2[i] == 14)) {
                                                s1 ++;
                                            }
                                            if (blocks[i] != blocks2[i]) mismatch++;
                                        }
                                    }
                                }

                                if (mismatch == 0) continue;

                                if (f1 > s1) {
                                    for (int rrx = rx-3; rrx <= rx + 3; rrx ++) {
                                        for (int rrz = rz - 3; rrz <= rz +3; rrz ++) {
                                            for (int y = 66; y <= 73; y++) {
                                                if (rrx >= wid) continue;
                                                if (rrz >= len) continue;
                                                if (rrx < 0) continue;
                                                if (rrz < 0) continue;

                                                int i = rrx + rrz * wid + y * wid * len;
                                                blocks2[i] = blocks[i];
                                                meta2[i] = meta[i];
                                            }
                                        }
                                    }
                                    changed = true;
                                }
                            }
                        }
                    }
                    if (changed) {
                        compound2 = CompoundBinaryTag.builder()
                                .put(compound2)
                                .putByteArray("Blocks", blocks2)
                                .putByteArray("Data",meta2).build();
                        String schm = FeatureCollectDungeonRooms.nbttostring("Schematic", compound2);
                        originalRoomMapping.remove("schematic");
                        originalRoomMapping.addProperty("schematic", schm);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        for (Map.Entry<String, JsonObject> stringJsonObjectEntry : roomMapping.entrySet()) {
            Gson gson = new Gson();
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(Files.newOutputStream(new File(outdir, stringJsonObjectEntry.getKey()+":"+ stringJsonObjectEntry.getValue().get("name").getAsString() +".dgrun").toPath())));
            gson.toJson(stringJsonObjectEntry.getValue(), writer);
            writer.flush();
            writer.close();
        }
    }


    public static class MechanicNameSuggestor implements SuggestionProvider<UCommandContext> {
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<UCommandContext> commandContext, SuggestionsBuilder suggestionsBuilder) {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            if (thePlayer == null) {
                return suggestionsBuilder.buildFuture();
            }
            Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

            DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
            for (String s : dungeonRoom.getMechanics().keySet()) {
                suggestionsBuilder.suggest(s);
            }
            return suggestionsBuilder.buildFuture();
        }
    }

    public static class StateSuggestor implements SuggestionProvider<UCommandContext> {
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<UCommandContext> commandContext, SuggestionsBuilder suggestionsBuilder) {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            if (thePlayer == null) {
                return suggestionsBuilder.buildFuture();
            }
            Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

            DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);

            String mechanic = commandContext.getArgument("mechanic", String.class);
            DungeonMechanicState state = dungeonRoom.getMechanics().get(mechanic);
            if (state == null) return suggestionsBuilder.buildFuture();

            for (String availableAction : state.getAvailableActions()) {
                suggestionsBuilder.suggest(availableAction);
            }
            return suggestionsBuilder.buildFuture();
        }
    }

    @DGCommand("dgdebug pathfind {mechanic} {state}")
    public void pathfindCommand(@CommandParam(value = "mechanic", suggestionProvider = MechanicNameSuggestor.class) String mechanic,
                                @CommandParam(value = "state", suggestionProvider = StateSuggestor.class) String state) {
        try {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            if (thePlayer == null) {
                return;
            }
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().tick();
            }
            Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

            DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
            RoomRouteHandler roomRouteHandler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(dungeonRoom);
            if (roomRouteHandler == null) return;

            roomRouteHandler.pathfind("COMMAND", mechanic, state, FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND::createPathDisplayEngine);
        } catch (Exception t) {
            t.printStackTrace();
        }
    }

    @DGCommand("dgdebug process")
    public void processCommand1() {
        File fileRoot = Main.getConfigDir();
        File dir = new File(fileRoot, "processorinput");
        File outsecret = new File(fileRoot, "processoroutsecret");
        CBORMapper cborMapper = new CBORMapper();
        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".roomdata.cbor")) {
                continue;
            }

            try {
                DungeonRoomInfo dri = cborMapper.readValue(f, DungeonRoomInfo.class);
                dri.setUserMade(false);
                cborMapper.writeValue(new File(outsecret, dri.getUuid().toString() + ".roomdata.cbor"), dri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @DGCommand("dgdebug check2")
    public void check2command() {
        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
            if (dungeonRoomInfo.getWorld() == null) {
                System.out.println("world null for " + dungeonRoomInfo.getName());
                continue;
            }
            for (DungeonMechanicData value : dungeonRoomInfo.getMechanics().values()) {
                if (value instanceof DungeonSecretEssenceState.DungeonSecretEssenceData) {
                    OffsetPoint offsetPoint = ((DungeonSecretEssenceState.DungeonSecretEssenceData) value).getSecretPoint();
                    if (!dungeonRoomInfo.getBlock(offsetPoint, 0).isOf(BlockType.SKULL)) {
                        System.out.println("setblock "+offsetPoint+" to skul on "+dungeonRoomInfo.getName());
                        dungeonRoomInfo.setBlock(offsetPoint, ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.SKULL));
                    }
                } else if (value instanceof DungeonRedstoneKeyState.DungeonRedstoneKeyData) {
                    OffsetPoint offsetPoint = ((DungeonRedstoneKeyState.DungeonRedstoneKeyData) value).getSecretPoint();
                    if (!dungeonRoomInfo.getBlock(offsetPoint, 0).isOf(BlockType.SKULL)) {
                        System.out.println("setblock "+offsetPoint+" to skul on "+dungeonRoomInfo.getName());
                        dungeonRoomInfo.setBlock(offsetPoint, ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.SKULL));
                    }
                } else if (value instanceof DungeonWizardCrystalState.DungeonWizardCrystalData) {
                    OffsetPoint offsetPoint = ((DungeonWizardCrystalState.DungeonWizardCrystalData) value).getSecretPoint();
                    if (!dungeonRoomInfo.getBlock(offsetPoint, 0).isOf(BlockType.SKULL)) {
                            System.out.println("setblock "+offsetPoint+" to skul on "+dungeonRoomInfo.getName());
                        dungeonRoomInfo.setBlock(offsetPoint, ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.SKULL));
                        }
                } else if (value instanceof DungeonSecretChestState.DungeonSecretChestData) {
                    OffsetPoint offsetPoint = ((DungeonSecretChestState.DungeonSecretChestData) value).getSecretPoint();
                    if (!dungeonRoomInfo.getBlock(offsetPoint, 0).isOf(BlockType.CHEST)) {
                            System.out.println("setblock "+offsetPoint+" to chest on "+dungeonRoomInfo.getName());
                        dungeonRoomInfo.setBlock(offsetPoint, ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.CHEST));
                        }
                } else if (value instanceof DungeonOnewayLeverState.DungeonOnewayLeverData) {
                    OffsetPoint offsetPoint = ((DungeonOnewayLeverState.DungeonOnewayLeverData) value).getLeverPoint();
                    if (!dungeonRoomInfo.getBlock(offsetPoint, 0).isOf(BlockType.LEVER)) {
                            System.out.println("setblock "+offsetPoint+" to lever on "+dungeonRoomInfo.getName());
                        dungeonRoomInfo.setBlock(offsetPoint, ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.LEVER));
                        }
                } else if (value instanceof DungeonSecretDoubleChestState.DungeonSecretDoubleChestData) {
                    OffsetPoint offsetPoint = ((DungeonSecretDoubleChestState.DungeonSecretDoubleChestData) value).getSecretPoint();
                    if (!dungeonRoomInfo.getBlock(offsetPoint, 0).isOf(BlockType.CHEST)) {
                            System.out.println("setblock "+offsetPoint+" to chest on "+dungeonRoomInfo.getName());
                        dungeonRoomInfo.setBlock(offsetPoint, ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.CHEST));
                        }
                    OffsetPoint offsetPoint2 = ((DungeonSecretDoubleChestState.DungeonSecretDoubleChestData) value).getSecretPoint2();
                    if (!dungeonRoomInfo.getBlock(offsetPoint, 0).isOf(BlockType.CHEST)) {
                            System.out.println("setblock "+offsetPoint2+" to chest on "+dungeonRoomInfo.getName());
                        dungeonRoomInfo.setBlock(offsetPoint, ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.CHEST));
                        }
                }
            }
        }
    }

    @DGCommand("dgdebug check")
    public void checkCommand() {
        File fileroot = new File(Main.getConfigDir(), "processorinput");
        CBORMapper cborMapper = new CBORMapper();
        for (File f : fileroot.listFiles()) {
            if (!f.getName().endsWith(".roomdata.cbor")) {
                continue;
            }
            try {
                DungeonRoomInfo dri = cborMapper.readValue(f, DungeonRoomInfo.class);
                System.out.println("Starting at " + dri.getName() + " - " + dri.getUuid());
                for (Map.Entry<String, DungeonMechanicData> value2 : dri.getMechanics().entrySet()) {
                    DungeonMechanicData value = value2.getValue();
                    if ((value instanceof DungeonSecretEssenceState.DungeonSecretEssenceData) && ((DungeonSecretEssenceState.DungeonSecretEssenceData) value).getSecretPoint().getY() == 0) {
                        OffsetPoint offsetPoint = ((DungeonSecretEssenceState.DungeonSecretEssenceData) value).getSecretPoint();
                        if (dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                            dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                            System.out.println("Fixing " + value2.getKey() + " - as secret " + value.getClass().getSimpleName() + " - at " + ((DungeonSecretEssenceState.DungeonSecretEssenceData) value).getSecretPoint());
                        }
                    } else  if ((value instanceof DungeonSecretChestState.DungeonSecretChestData) && ((DungeonSecretChestState.DungeonSecretChestData) value).getSecretPoint().getY() == 0) {
                        OffsetPoint offsetPoint = ((DungeonSecretChestState.DungeonSecretChestData) value).getSecretPoint();
                        if (dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                            dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                            System.out.println("Fixing " + value2.getKey() + " - as secret " + value.getClass().getSimpleName() + " - at " + ((DungeonSecretChestState.DungeonSecretChestData) value).getSecretPoint());
                        }
                    } else if (value instanceof DungeonOnewayDoorState.DungeonOnewayDoorData) {
                        for (OffsetPoint offsetPoint : ((DungeonOnewayDoorState.DungeonOnewayDoorData) value).getSecretPoint().getOffsetPointList()) {
                            if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                System.out.println("Fixing " + value2.getKey() + " - o-door - at " + offsetPoint);
                            }
                        }
                    } else if (value instanceof DungeonDoorState.DungeonDoorData) {
                        for (OffsetPoint offsetPoint : ((DungeonDoorState.DungeonDoorData) value).getSecretPoint().getOffsetPointList()) {
                            if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                System.out.println("Fixing " + value2.getKey() + " - door - at " + offsetPoint);
                            }
                        }
                    } else if (value instanceof DungeonBreakableWallState.DungeonBreakableWallData) {
                        for (OffsetPoint offsetPoint : ((DungeonBreakableWallState.DungeonBreakableWallData) value).getSecretPoint().getOffsetPointList()) {
                            if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                System.out.println("Fixing " + value2.getKey() + " - wall - at " + offsetPoint);
                            }
                        }
                    } else if (value instanceof DungeonTombState.DungeonTombData) {
                        for (OffsetPoint offsetPoint : ((DungeonTombState.DungeonTombData) value).getSecretPoint().getOffsetPointList()) {
                            if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                System.out.println("Fixing " + value2.getKey() + " - crypt - at " + offsetPoint);
                            }
                        }
                    }
                }
                cborMapper.writeValue(f, dri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @DGCommand("dgdebug reloaddungeon")
    public void reloadDungeonCommand() {
        try {
            ModAPI.getAPI().getEventBus().fireEvent(new DungeonLeftEvent());

            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
            MapUtils.clearMap();
        } catch (Exception t) {
            t.printStackTrace();
        }
    }

    @DGCommand("dgdebug partyid")
    public void partyIdCommand() {
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fInternal Party id: " + Optional.ofNullable(PartyManager.INSTANCE.getPartyContext()).map(PartyContext::getPartyID).orElse(null));
    }

    @DGCommand("dgdebug loc")
    public void locCommand() {
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fYou're in " + SkyblockStatus.getLocationName());
    }

    @DGCommand("dgdebug saverun")
    public void saveRunCommand() {
        try {
            File f = Main.getConfigDir();
            File runDir = new File(f, "dungeonruns");
            runDir.mkdirs();

            File runFile = new File(runDir, UUID.randomUUID() + ".dgrun");

            DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (dungeonContext == null) {
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cCouldn't find dungeon to save!");
                return;
            }
            DungeonEventHolder dungeonEventHolder = new DungeonEventHolder();
            dungeonEventHolder.setDate(dungeonContext.getInit());
            dungeonEventHolder.setPlayers(dungeonContext.getPlayers());
            dungeonEventHolder.setEventDataList(dungeonContext.getRecorder().getEvents());


            ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(runFile.toPath()));
            oos.writeObject(dungeonEventHolder);
            oos.flush();
            oos.close();
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fSuccessfully saved dungeon run to " + runFile.getAbsolutePath());
        } catch (Exception e) {
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cAn error occured while writing rundata " + e.getMessage());
            e.printStackTrace();
        }
    }

    @DGCommand("dgdebug requeststaticresource {uuid}")
    public void requestStaticResource(String uuid) {
        UUID uid = UUID.fromString(uuid);
        StaticResourceCache.INSTANCE.getResource(uid).thenAccept(a -> {
            ChatTransmitter.addToQueue(a.getResourceID() + ": " + a.getValue() + ": " + a.isExists());
        });
    }

    @DGCommand("dgdebug transferschematic {ignoreAir}")
    public void transferSchematic(boolean ignoreAir) {
        FeatureRegistry.ADVANCED_ROOMEDIT.overwrite(ignoreAir);
        ChatTransmitter.sendDebugChat("TRANSFERRED SCHEMATIC");
    }

    @DGCommand("dgdebug closecontext")
    public void closeContextCommand() {
        DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(false);

        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
    }

    @DGCommand("dgdebug dumpsettings")
    public void dumpSettingsCommand() {
        for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
            System.out.println(abstractFeature.getCategory()+"\t"+abstractFeature.getName());
        }

//        NestedCategory nestedCategory = new NestedCategory("ROOT");
//        for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
//            String category = abstractFeature.getCategory();
//            NestedCategory currentRoot = nestedCategory;
//            for (String s : category.split("\\.")) {
//                NestedCategory finalCurrentRoot = currentRoot;
//                if (currentRoot.children().containsKey(s)) {
//                    currentRoot = currentRoot.children().get(s);
//                } else {
//                    currentRoot.child(currentRoot = new NestedCategory(finalCurrentRoot.categoryFull() + "." + s));
//                }
//            }
//        }
//
//        StringBuilder stringBuilder = new StringBuilder();
//        StringBuilder stringBuilder2 = new StringBuilder();
//
//        Stack<Tuple<NestedCategory, Integer>> stak = new Stack<>();
//        stak.push(new Tuple<>(nestedCategory, 0));
//        Set<NestedCategory> discovered = new HashSet<>();
//        while (!stak.isEmpty()) {
//            Tuple<NestedCategory, Integer> n = stak.pop();
//            if (discovered.contains(n.getFirst())) {
//                continue;
//            }
//            discovered.add(n.getFirst());
//            for (Map.Entry<String, NestedCategory> stringNestedCategoryEntry : n.getFirst().children().entrySet()) {
//                stak.push(new Tuple<>(stringNestedCategoryEntry.getValue(), n.getSecond() + 1));
//            }
//
//            if (n.getFirst().categoryFull().equals("ROOT")) {
//                continue;
//            }
//
//            String prefix = "";
//            for (int i = 0; i < n.getSecond() - 1; i++) {
//                prefix += "    ";
//            }
//
//            List<AbstractFeature> abstractFeatureList = FeatureRegistry.getFeaturesByCategory().getOrDefault(n.getFirst().categoryFull().substring(5), Collections.emptyList());
//            stringBuilder.append(prefix).append("- C ").append(n.getFirst().categoryFull()).append("\n");
//            stringBuilder2.append(n.getFirst().categoryFull()).append("\n");
//            for (AbstractFeature abstractFeature : abstractFeatureList) {
//                stringBuilder.append(prefix).append("    - F ").append(abstractFeature.getName()).append(" / ").append(abstractFeature.getDescription().replace("\n", "$NEW_LINE$")).append("\n");
//            }
//        }
//        System.out.println(stringBuilder.toString());
//        System.out.println(stringBuilder2.toString());
    }
    @DGCommand("dgdebug readmap {x} {y}")
    public void readMapCommand(int x, int y) {
        try {
            ChatTransmitter.addToQueue(MapUtils.readDigit(MapUtils.getColors(), x, y) + "-");
/*                int cntY = Integer.parseInt(args[3]);
                int target = Integer.parseInt(args[4]);
                StringBuilder sb = new StringBuilder("{");
                for (int y = fromY; y < fromY + cntY; y++) {
                    int curr = 0;
                    for (int x = fromX; x < fromX+8; x++) {
                        byte clr = MapUtils.getMapColorAt(MapUtils.getColors(), x,y);
                        if (clr == target) curr = (curr << 1) | 1;
                        else curr <<= 1;
                    }
                    sb.append("0x").append(Integer.toHexString(curr).toUpperCase());
                    if (y != fromY + cntY - 1) sb.append(", ");
                }
                sb.append("}");
                System.out.println("\n"+sb.toString());*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DGCommand("dgdebug testgui")
    public void testGuiCommand() {
        GuiScreenAdapter adapter = new GuiScreenAdapter(new TestView());
        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
                Minecraft.getMinecraft().displayGuiScreen(adapter);
            });
        }).start();
    }

    @DGCommand("dgdebug clearprofile")
    public void clearProfileCommand() {
        ModAPI.getAPI().getProfiler().clearprofiling();
    }

    @DGCommand("dgdebug fullbright {gamma}")
    public void fullBrightCommand(int gamma) {
        ModAPI.getAPI().getGameSettings().setGamma(gamma);
    }
    @DGCommand("dgdebug fullbright")
    public void fullBright() {
        ModAPI.getAPI().getGameSettings().setGamma(1000);
    }

    @DGCommand("dgdebug pfall")
    public void pFallCommand() {
        try {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            if (thePlayer == null) {
                return;
            }
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().tick();
            }
            Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

            DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);

            RoomRouteHandler roomRouteHandler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(dungeonRoom);
            if (roomRouteHandler == null) return;
            // performance testing (lol)
            for (String s : dungeonRoom.getMechanics().keySet()) {
                roomRouteHandler.pathfind("COMMAND-" + s, s, "navigate", FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND::createPathDisplayEngine);
            }
        } catch (Exception t) {
            t.printStackTrace();
        }
    }

    @DGCommand("dgdebug partycollection {otherPlayerName} {fragbot} {offline}")
    public void partyCollectionCommand(String otherPlayerName, String fragbot, String offline) {

        String sourcePlayer = ModAPI.getAPI().getPlayer().getName();
        String targetPlayer = otherPlayerName;
        String thirdPlayer = fragbot;
        String offlinePlayer = offline;
            StringBuilder sb = new StringBuilder();
            Consumer writer = (obj) -> {
                sb.append("\n***************************\n");
                if (obj instanceof List) {
                    for (Object obj2: (List) obj) {
                        sb.append("\n> ");
                        sb.append(obj2);
                    }
                } else {
                    sb.append("\n> ");
                    sb.append(obj);
                }
                sb.append("\n");
            };

            new ChatRoutine() {
                @Override
                public void run() {
                    String langs = "ENGLISH, GERMAN, FRENCH, DUTCH, SPANISH, ITALIAN, CHINESE_SIMPLIFIED, CHINESE_TRADITIONAL, PORTUGUESE_BR, RUSSIAN, KOREAN, POLISH, JAPANESE, PIRATE, NORWEGIAN, PORTUGUESE_PT, SWEDISH, TURKISH, DANISH, CZECH, FINNISH, GREEK, UKRAINIAN, ROMANIAN, HUNGARIAN";
                    for (String s : langs.split(",")) {
                        say("/lang "+s.trim());
                        waitForSingleMessageMatching(a -> a.startsWith("§a"), (a) -> {});
                        justWait(500);
                        justRun(() -> writer.accept("\n\n$$LANGUAGE$$: "+s+"\n\n"));
                        rejoinHypickle();

                        say("/p leave");
                        say("/chat a");

                        otherSay("/p "+sourcePlayer);
                        waitForSingleMessageMatching(a -> a.startsWith("§9§m-----------------------------------------------------"), (a) -> {});
                        say("/p accept "+targetPlayer);
                        waitForPartyMessage((a) -> {});

                        otherSay("/p promote "+sourcePlayer);
                        waitForPartyMessage((a) -> {});
                        otherSay("/p "+thirdPlayer);
                        waitForPartyMessage((a) -> {});
                        waitForPartyMessage((a) -> {});

                        otherSay("/p leave");
                        waitForPartyMessage(writer);

                        say("/p disband");
                        //~ §ehas disbanded the party!§r
                        waitForPartyMessage(writer);


                        say("/p settings allinvite");
                        // §cYou are not currently in a party.§r
                        waitForPartyMessage(writer);
                        say("/p mute");
                        // §cYou are not in a party!§r
                        waitForPartyMessage(writer);
                        say("/p disband");
                        // §cYou are not in a party right now.§r
                        waitForPartyMessage(writer);
                        say("/chat p");
                        // §cYou must be in a party to join the party channel!§r
                        waitForPartyMessage(writer);



                        say("/p "+targetPlayer);
                        // §b[MVP§r§a+§r§b] syeyoung §r§einvited §r§b[MVP§r§0+§r§b] Azael_Nya §r§eto the party! They have §r§c60 §r§eseconds to accept.§r
                        waitForPartyMessage(writer);
                        justWait(500);

                        otherSay("/p accept syeyoung");
                        // §b[MVP§r§0+§r§b] Azael_Nya §r§ejoined the party.§r
                        waitForPartyMessage(writer);

                        say("/chat p");
                        // §aYou are now in the §r§6PARTY§r§a channel§r
                        waitForSingleMessageMatching((a) -> a.startsWith("§a") || a.startsWith("§6"), writer);


                        say("/p settings allinvite");
                        // §b[MVP§r§a+§r§b] syeyoung §r§aenabled All Invite§r
                        waitForPartyMessage(writer);
                        say("/p settings allinvite");
                        // §b[MVP§r§a+§r§b] syeyoung §r§cdisabled All Invite§r
                        waitForPartyMessage(writer);

                        say("/p 99999999999999999");
                        // §cCouldn't find a player with that name!§r
                        waitForPartyMessage(writer);
                        say("/p "+offlinePlayer);
                        // §cYou cannot invite that player since they're not online.
                        waitForPartyMessage(writer);


                        say("/p promote "+targetPlayer);
                        // §b[MVP§r§f+§r§b] apotato321§r§e has promoted §r§a[VIP§r§6+§r§a] syeyoung §r§eto Party Moderator§r
                        waitForPartyMessage(writer);
                        say("/p promote "+targetPlayer);
                        // §a[VIP§r§6+§r§a] syeyoung§r§e has promoted §r§b[MVP§r§f+§r§b] apotato321 §r§eto Party Leader§r
                        waitForPartyMessage(writer);
                        otherSay("/p demote "+sourcePlayer);
                        // §b[MVP§r§a+§r§b] syeyoung§r§e has demoted §r§b[MVP§r§0+§r§b] Azael_Nya §r§eto Party Member§r
                        waitForPartyMessage(writer);
                        otherSay("/p transfer "+sourcePlayer);
                        // §eThe party was transferred to §r§b[MVP§r§f+§r§b] apotato321 §r§eby §r§a[VIP§r§6+§r§a] syeyoung§r
                        waitForPartyMessage(writer);

                        // leaves
                        otherSay("/p leave");
                        // §b[MVP§r§0+§r§b] Azael_Nya §r§ehas left the party.§r
                        waitForPartyMessage(writer);

                        otherSay("/p "+thirdPlayer);
                        // §cThe party was disbanded because all invites expired and the party was empty.§r
                        waitForPartyMessage(writer);

                        say("smth");
                        // §cYou are not in a party and were moved to the ALL channel.§r
                        waitForPartyMessage(writer);

                        otherSay("/p "+sourcePlayer);
                        // §r§b[MVP§r§0+§r§b] Azael_Nya §r§ehas invited you to join their party!
                        // §r§eYou have §r§c60 §r§eseconds to accept. §r§6Click here to join!§r§9
                        waitForSingleMessageMatching(a -> a.startsWith("§9§m-----------------------------------------------------"), writer);

                        justWait(1000);

                        say("/p "+targetPlayer);
                        // §eYou have joined §r§b[MVP§r§0+§r§b] Azael_Nya's §r§eparty!§r
                        waitForPartyMessage(writer);

                        say("/p "+offlinePlayer);
                        // §cYou are not allowed to invite players.§r
                        waitForPartyMessage(writer);

                        otherSay("/p kick "+thirdPlayer);
                        // §ehas been removed from the party.§r
                        waitForPartyMessage(writer);
                        otherSay("/p kick "+sourcePlayer);
                        // §eYou have been kicked from the party by
                        waitForPartyMessage(writer);

                        // invite
                        say("/p "+targetPlayer);
                        waitForPartyMessage((a) -> {});
                        justWait(500);
                        otherSay("/p accept "+sourcePlayer);
                        waitForPartyMessage((a) -> {});

                        say("/pl");
                        // §6Party Members
                        waitForPartyMessage(writer);
                        say("/p leave");
                        // §eYou left the party.§r
                        waitForPartyMessage(writer);
                        // --disbanded--
                        waitForPartyMessage((a) -> {});

                        justRun(() -> {

                            try {
                                String total = sb.toString();
                                FileOutputStream fos = new FileOutputStream("partymessages.txt");
                                fos.write(total.getBytes());
                                fos.flush();
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });

                    }

                    say("/lang ENGLISH");

                }
            }.execute();



    }
}
