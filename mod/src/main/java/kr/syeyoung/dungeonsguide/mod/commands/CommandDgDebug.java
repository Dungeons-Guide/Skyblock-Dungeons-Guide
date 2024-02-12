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

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatRoutine;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.MainConfigWidget;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.DungeonEventHolder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDungeonRooms;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.guiv2.view.TestView;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Score;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.utils.AhUtils;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class CommandDgDebug extends CommandBase {
    @Override
    public String getCommandName() {
        return "dgdebug";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {

        return "dgdebug";
    }

    //List of subcommands for tab support
    private static final String[] SUBCOMMANDS = {
            "scoreboard",
            "scoreboardclean",
            "tablist",
            "mockdungeonstart",
            "saverooms",
            "loadrooms",
            "reloadah",
            "brand",
            "pathfind",
            "process",
            "check",
            "reloaddungeon",
            "partyid",
            "loc",
            "saverun",
            "requeststaticresource",
            "createfakeroom",
            "closecontext",
            "dumpsettings",
            "readmap",
            "testgui",
            "clearprofile",
            "fullbright",
            "gimmebright",
            "pfall",
            "partycollection"
    };

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        }
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            return;
        }

        switch (args[0].toLowerCase()) { //Case Insensitive
            case "freeze":
                while(true);
            case "scoreboard":
                scoreboardCommand();
                break;
            case "scoreboardclean":
                scoreboardCleanCommand();
                break;
            case "tablist":
                tabListCommand();
                break;
            case "mockdungeonstart":
                mockDungeonStartCommand(args);
                break;
            case "saverooms":
                saveRoomsCommand();
                break;
            case "loadrooms":
                loadRoomsCommand();
                break;
            case "reloadah":
                reloadAHCommand();
                break;
            case "brand":
                brandCommand();
                break;
            case "pathfind":
                pathfindCommand(args);
                break;
            case "process":
                processCommand1();
                break;
            case "process2":
                process2();
                break;
            case "groupprocess":
                try {
                    groupprocess();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case "nodupeprocess":
                try {
                    removedupe();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case "removedoors":
                try {
                    removedoors();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case "check":
                checkCommand();
                break;
            case "reloaddungeon":
                reloadDungeonCommand();
                break;
            case "partyid":
                partyIdCommand();
                break;
            case "loc":
                locCommand();
                break;
            case "saverun":
                saveRunCommand();
                break;
            case "requeststaticresource":
                requestStaticResource(args);
                break;
            case "transferschematic": // load schematic
                transferSchematic(Boolean.parseBoolean(args.length == 1 ? "true" : args[1]));
                break;
            case "closecontext":
                closeContextCommand();
                break;
            case "re":
                MainConfigWidget mainConfigWidget = new MainConfigWidget();
                GuiScreenAdapter adapter = new GuiScreenAdapter(new GlobalHUDScale(
                        FeatureRegistry.ADVANCED_ROOMEDIT.getConfigureWidget()
                ));

                DungeonsGuide.getDungeonsGuide().getCommandDungeonsGuide().setTarget(adapter);

                break;
            case "dumpsettings":
                dumpSettingsCommand();
                break;
            case "readmap":
                readMapCommand(args);
                break;
            case "testgui":
                testGuiCommand();
                break;
            case "clearprofile":
                clearProfileCommand();
                break;
            case "fullbright":
            case "gimmebright":
                fullBrightCommand(args);
                break;
            case "pfall":
                pFallCommand();
                break;
            case "reloadshader":
                ShaderManager.onResourceReload();
                break;
            case "partycollection":
                partyCollectionCommand(args[1], args[2], args[3]);
                break;
            default:
                ChatTransmitter.addToQueue(new ChatComponentText("ain't gonna find much anything here"));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg loadrooms §7-§f Reloads dungeon roomdata."));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg brand §7-§f View server brand."));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg info §7-§f View Current DG User info."));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverun §7-§f Save run to be sent to developer."));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverooms §7-§f Saves usergenerated dungeon roomdata."));
                break;
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    //BEGIN COMMANDS FROM ARGS[0]

    private void scoreboardCommand() {
        for (Score score : ScoreboardManager.INSTANCE.getSidebarObjective().getScores()) {
            ChatTransmitter.addToQueue("LINE: " + score.getVisibleName() + ": " + score.getScore());
        }
    }

    private void scoreboardCleanCommand() {
        for (Score score : ScoreboardManager.INSTANCE.getSidebarObjective().getScores()) {
            ChatTransmitter.addToQueue("LINE: " + score.getJustTeam() + ": " + score.getScore());
        }
    }

    private void tabListCommand() {
        for (TabListEntry entry : TabList.INSTANCE.getTabListEntries()) {
            ChatTransmitter.addToQueue(entry.getFormatted() + " " + entry.getEffectiveName() + "(" + entry.getPing() + ")" + entry.getGameMode());
        }
        ChatTransmitter.addToQueue("VS");
    }

    private void mockDungeonStartCommand(String[] args) {
        if (!Minecraft.getMinecraft().isSingleplayer()) {
            ChatTransmitter.addToQueue("This only works in singlepauer", false);
            return;
        }

        if (args.length == 2) {
            int time = Integer.parseInt(args[1]);
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

    private void saveRoomsCommand() {
        DungeonRoomInfoRegistry.saveAll(new File(Main.getConfigDir(), "roomdatas"));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved user generated roomdata"));
    }

    private void process2() {
//        try {
//            Field f=  ValueEditRegistry.class.getDeclaredField("valueEditMap");
//            f.setAccessible(true);
//            Map map = (Map) f.get(null);
//            map.put(DungeonWizard.class.getName(), new ValueEditWizard.Generator());
//
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
            System.out.println(dungeonRoomInfo.getName()+"-------");
            for (Map.Entry<String, DungeonMechanic> entry : dungeonRoomInfo.getMechanics().entrySet()) {
                if (entry.getValue() instanceof DungeonDoor) {
                    System.out.println(entry.getKey()+"/"+((DungeonDoor) entry.getValue()).getOpenPreRequisite()+"/"+((DungeonDoor) entry.getValue()).getClosePreRequisite()+"/"+((DungeonDoor) entry.getValue()).getMovePreRequisite());
                } else if (entry.getValue() instanceof DungeonOnewayDoor){
                    System.out.println(entry.getKey()+"/"+((DungeonOnewayDoor) entry.getValue()).getPreRequisite() +"/"+((DungeonOnewayDoor) entry.getValue()).getMovePreRequisite());
                }
            }
        }
    }

    private void loadRoomsCommand() {
        try {
            DungeonRoomInfoRegistry.loadAll(new File(Main.getConfigDir(), "roomdatas"));
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully loaded roomdatas"));
            return;
        } catch (BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException |
                 NoSuchAlgorithmException | IOException | IllegalBlockSizeException |
                 NoSuchPaddingException e) {
            e.printStackTrace();
        }
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cAn error has occurred while loading roomdata"));
    }

    private void reloadAHCommand() {
        try {
            AhUtils.loadAuctions();
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException |
                 InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException |
                 KeyStoreException | IllegalBlockSizeException | KeyManagementException e) {
            e.printStackTrace();
        }
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fReloaded Ah data"));
    }

    private void brandCommand() {
        String serverBrand = Minecraft.getMinecraft().thePlayer.getClientBrand();
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e" + serverBrand));
    }
    private void removedoors() throws Exception {
        File fileRoot = Main.getConfigDir();
        File dir = new File(fileRoot, "grouped2");
        File outdir = new File(fileRoot, "grouped3");


        Iterator<File> fileIter = FileUtils.iterateFiles(dir, new String[] {"dgrun"}, true);

        while (fileIter.hasNext()) {

            try {
                File f = fileIter.next();
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(IOUtils.toString(f.toURI()), JsonObject.class);


                NBTTagCompound compound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(
                        jsonObject.get("schematic").getAsString()
                )));
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
                compound.setByteArray("Blocks", blocks);
                compound.setByteArray("Data", meta);

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

    private void removedupe() throws Exception  {

        File fileRoot = Main.getConfigDir();
        File dir = new File(fileRoot, "grouped");
        File outdir = new File(fileRoot, "grouped2");

        Iterator<File> fileIter = FileUtils.iterateFiles(dir, new String[] {"dgrun"}, true);

        while (fileIter.hasNext()) {
            try {
                File f = fileIter.next();
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(IOUtils.toString(f.toURI()), JsonObject.class);


                NBTTagCompound compound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(
                        jsonObject.get("schematic").getAsString()
                )));
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
    private void groupprocess() throws Exception {
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


                NBTTagCompound compound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(
                        jsonObject.get("schematic").getAsString()
                )));
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
                    compound.setByteArray("Blocks", blocks);
                    compound.setByteArray("Data", meta);

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


                    NBTTagCompound compound2 = CompressedStreamTools.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(
                            originalRoomMapping.get("schematic").getAsString()
                    )));
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
                        compound2.setByteArray("Blocks", blocks2);
                        compound2.setByteArray("Data", meta2);

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

    private void pathfindCommand(String[] args) {
        try {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer == null) {
                return;
            }
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().tick();
            }
            Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());

            DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
            GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
            grp.pathfind("COMMAND", args[1], args[2], FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.getRouteProperties());
        } catch (Exception t) {
            t.printStackTrace();
        }
    }

    private void processCommand1() {
        File fileRoot = Main.getConfigDir();
        File dir = new File(fileRoot, "processorinput");
        File outsecret = new File(fileRoot, "processoroutsecret");
        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".roomdata")) {
                continue;
            }
            try {
                InputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                DungeonRoomInfo dri = (DungeonRoomInfo) ois.readObject();
                ois.close();
                fis.close();
                dri.setUserMade(false);

                FileOutputStream fos = new FileOutputStream(new File(outsecret, dri.getUuid().toString() + ".roomdata"));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(dri);
                oos.flush();
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkCommand() {
        File fileroot = new File(Main.getConfigDir(), "processorinput");
        for (File f : fileroot.listFiles()) {
            if (!f.getName().endsWith(".roomdata")) {
                continue;
            }
            try {
                InputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                DungeonRoomInfo dri = (DungeonRoomInfo) ois.readObject();
                ois.close();
                fis.close();
                System.out.println("Starting at " + dri.getName() + " - " + dri.getUuid());
                for (Map.Entry<String, DungeonMechanic> value2 : dri.getMechanics().entrySet()) {
                    DungeonMechanic value = value2.getValue();
                    if (value instanceof DungeonSecret &&
                            (((DungeonSecret) value).getSecretType() == DungeonSecret.SecretType.BAT
                                    || ((DungeonSecret) value).getSecretType() == DungeonSecret.SecretType.CHEST)
                            && ((DungeonSecret) value).getSecretPoint().getY() == 0) {
                        OffsetPoint offsetPoint = ((DungeonSecret) value).getSecretPoint();
                        if (dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                            dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                            System.out.println("Fixing " + value2.getKey() + " - as secret " + ((DungeonSecret) value).getSecretType() + " - at " + ((DungeonSecret) value).getSecretPoint());
                        }
                    } else if (value instanceof DungeonOnewayDoor) {
                        for (OffsetPoint offsetPoint : ((DungeonOnewayDoor) value).getSecretPoint().getOffsetPointList()) {
                            if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                System.out.println("Fixing " + value2.getKey() + " - o-door - at " + offsetPoint);
                            }
                        }
                    } else if (value instanceof DungeonDoor) {
                        for (OffsetPoint offsetPoint : ((DungeonDoor) value).getSecretPoint().getOffsetPointList()) {
                            if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                System.out.println("Fixing " + value2.getKey() + " - door - at " + offsetPoint);
                            }
                        }
                    } else if (value instanceof DungeonBreakableWall) {
                        for (OffsetPoint offsetPoint : ((DungeonBreakableWall) value).getSecretPoint().getOffsetPointList()) {
                            if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                System.out.println("Fixing " + value2.getKey() + " - wall - at " + offsetPoint);
                            }
                        }
                    } else if (value instanceof DungeonTomb) {
                        for (OffsetPoint offsetPoint : ((DungeonTomb) value).getSecretPoint().getOffsetPointList()) {
                            if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                System.out.println("Fixing " + value2.getKey() + " - crypt - at " + offsetPoint);
                            }
                        }
                    }
                }
                FileOutputStream fos = new FileOutputStream(f);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(dri);
                oos.flush();
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void reloadDungeonCommand() {
        try {
            MinecraftForge.EVENT_BUS.post(new DungeonLeftEvent());

            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
            MapUtils.clearMap();
        } catch (Exception t) {
            t.printStackTrace();
        }
    }

    private void partyIdCommand() {
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fInternal Party id: " + Optional.ofNullable(PartyManager.INSTANCE.getPartyContext()).map(PartyContext::getPartyID).orElse(null)));
    }

    private void locCommand() {
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fYou're in " + SkyblockStatus.locationName));
    }

    private void saveRunCommand() {
        try {
            File f = Main.getConfigDir();
            File runDir = new File(f, "dungeonruns");
            runDir.mkdirs();

            File runFile = new File(runDir, UUID.randomUUID() + ".dgrun");

            DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (dungeonContext == null) {
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cCouldn't find dungeon to save!"));
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
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved dungeon run to " + runFile.getAbsolutePath()));
        } catch (Exception e) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cAn error occured while writing rundata " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void requestStaticResource(String[] args) {
        UUID uid = UUID.fromString(args[1]);
        StaticResourceCache.INSTANCE.getResource(uid).thenAccept(a -> {
            ChatTransmitter.addToQueue(new ChatComponentText(a.getResourceID() + ": " + a.getValue() + ": " + a.isExists()));
        });
    }

    private void transferSchematic(boolean ignoreAir) {
        FeatureRegistry.ADVANCED_ROOMEDIT.overwrite(ignoreAir);
        ChatTransmitter.sendDebugChat("TRANSFERRED SCHEMATIC");
    }

    private void closeContextCommand() {
        DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(false);

        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
    }

    private void dumpSettingsCommand() {
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

    private void readMapCommand(String[] args) {
        try {
            int fromX = Integer.parseInt(args[1]);
            int fromY = Integer.parseInt(args[2]);
            ChatTransmitter.addToQueue(new ChatComponentText(MapUtils.readDigit(MapUtils.getColors(), fromX, fromY) + "-"));
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

    private void testGuiCommand() {
        GuiScreenAdapter adapter = new GuiScreenAdapter(new TestView());
        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft.getMinecraft().displayGuiScreen(adapter);
            });
        }).start();
    }

    private void clearProfileCommand() {
        Minecraft.getMinecraft().mcProfiler.clearProfiling();
    }

    private void fullBrightCommand(String[] args) {
        int gammaVal = 1000;
        if (args.length == 2) {
            try {
                gammaVal = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                ChatTransmitter.addToQueue(new ChatComponentText("Invalid number, defaulting to 1000"));
            }
        }
        Minecraft.getMinecraft().gameSettings.setOptionFloatValue(GameSettings.Options.GAMMA, gammaVal);
    }

    private void pFallCommand() {
        try {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer == null) {
                return;
            }
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().tick();
            }
            Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());

            DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
            GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
            // performance testing (lol)
            for (String s : dungeonRoom.getMechanics().keySet()) {
                grp.pathfind("COMMAND-" + s, s, "navigate", FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.getRouteProperties());
            }
        } catch (Exception t) {
            t.printStackTrace();
        }
    }

    private void partyCollectionCommand(String otherPlayerName, String fragbot, String offline) {

        String sourcePlayer = Minecraft.getMinecraft().thePlayer.getName();
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
                        waitForSingleMessageMatching(a -> a.startsWith("§r§a"), (a) -> {});
                        justWait(500);
                        justRun(() -> writer.accept("\n\n$$LANGUAGE$$: "+s+"\n\n"));
                        rejoinHypickle();

                        say("/p leave");
                        say("/chat a");

                        otherSay("/p "+sourcePlayer);
                        waitForSingleMessageMatching(a -> a.startsWith("§9§m-----------------------------------------------------§r§9"), (a) -> {});
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
                        waitForSingleMessageMatching(a -> a.startsWith("§9§m-----------------------------------------------------§r§9"), writer);

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
