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

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.NestedCategory;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.DungeonEventHolder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.view.TestView;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Score;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.AhUtils;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.MinecraftForge;

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

public class CommandDgDebug extends CommandBase {
    @Override
    public String getCommandName() {
        return "dgdebug";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {

        return "dgdebug";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) return;
        String arg = args[0].toLowerCase();


        if ("scoreboard".equals(arg)) {
            for (Score score : ScoreboardManager.INSTANCE.getSidebarObjective().getScores()) {
                ChatTransmitter.addToQueue("LINE: " + score.getVisibleName()+": "+score.getScore());
            }
        } else if ("scoreboardclean".equals(arg)) {
            for (Score score : ScoreboardManager.INSTANCE.getSidebarObjective().getScores()) {
                ChatTransmitter.addToQueue("LINE: " + score.getJustTeam()+": "+score.getScore());
            }
        }else if ("tablist".equals(arg)) {
            for (TabListEntry entry : TabList.INSTANCE.getTabListEntries()) {
                ChatTransmitter.addToQueue(entry.getFormatted()+" "+entry.getEffectiveName()+"("+entry.getPing()+")" + entry.getGamemode());
            }
            ChatTransmitter.addToQueue("VS");
        } else if ("mockdungeonstart".equals(arg)) {
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
        } else if ("saverooms".equals(arg)) {
            DungeonRoomInfoRegistry.saveAll(Main.getConfigDir());
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved user generated roomdata"));
        } else if ("loadrooms".equals(arg)) {
            try {
                DungeonRoomInfoRegistry.loadAll(Main.getConfigDir());
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully loaded roomdatas"));
                return;
            } catch (BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException |
                     NoSuchAlgorithmException | IOException | IllegalBlockSizeException |
                     NoSuchPaddingException e) {
                e.printStackTrace();
            }
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cAn error has occurred while loading roomdata"));
        } else if ("reloadah".equals(arg)) {
            try {
                AhUtils.loadAuctions();
            } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException |
                     InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException |
                     KeyStoreException | IllegalBlockSizeException | KeyManagementException e) {
                e.printStackTrace();
            }
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fReloaded Ah data"));
        } else if ("brand".equals(arg)) {
            String serverBrand = Minecraft.getMinecraft().thePlayer.getClientBrand();
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e" + serverBrand));

        } else if ("pathfind".equals(arg)) {
            try {
                DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                if (thePlayer == null) return;
                if (context.getBossfightProcessor() != null) context.getBossfightProcessor().tick();
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
                grp.pathfind("COMMAND", args[1], args[2], FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.getRouteProperties());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if ("process".equals(arg)) {
            File fileRoot = Main.getConfigDir();
            File dir = new File(fileRoot, "processorinput");
            File outsecret = new File(fileRoot, "processoroutsecret");
            for (File f : dir.listFiles()) {
                if (!f.getName().endsWith(".roomdata")) continue;
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
        } else if ("check".equals(arg)) {
            File fileroot = Main.getConfigDir();
            for (File f : fileroot.listFiles()) {
                if (!f.getName().endsWith(".roomdata")) continue;
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
        } else if ("reloaddungeon".equals(arg)) {
            try {
                MinecraftForge.EVENT_BUS.post(new DungeonLeftEvent());

                DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
                MapUtils.clearMap();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if ("partyid".equals(arg)) {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fInternal Party id: " + Optional.ofNullable(PartyManager.INSTANCE.getPartyContext()).map(PartyContext::getPartyID).orElse(null)));
        } else if ("loc".equals(arg)) {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fYou're in " + SkyblockStatus.locationName));
        } else if ("saverun".equals(arg)) {
            try {
                File f = Main.getConfigDir();
                File runDir = new File(f, "dungeonruns");
                runDir.mkdirs();

                File runFile = new File(runDir, UUID.randomUUID() + ".dgrun");

                DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                if (dungeonContext == null) {
                    sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cCouldn't find dungeon to save!"));
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
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved dungeon run to " + runFile.getAbsolutePath()));
            } catch (Exception e) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cAn error occured while writing rundata " + e.getMessage()));
                e.printStackTrace();
            }
        } else if ("requeststaticresource".equals(arg)) {
            UUID uid = UUID.fromString(args[1]);
            StaticResourceCache.INSTANCE.getResource(uid).thenAccept(a -> {
                sender.addChatMessage(new ChatComponentText(a.getResourceID() + ": " + a.getValue() + ": " + a.isExists()));
            });
        } else if ("createfakeroom".equals(arg)) {// load schematic
//            File f = new File(Main.getConfigDir(), "schematics/new roonm-b2df250c-4af2-4201-963c-0ee1cb6bd3de-5efb1f0c-c05f-4064-bde7-cad0874fdf39.schematic");
//            NBTTagCompound compound;
//            try {
//                compound = CompressedStreamTools.readCompressed(new FileInputStream(f));
//            } catch (IOException e) {
//                e.printStackTrace();
//                return;
//            }
//
//            byte[] blocks = compound.getByteArray("Blocks");
//            byte[] meta = compound.getByteArray("Data");
//            for (int x = 0; x < compound.getShort("Width"); x++) {
//                for (int y = 0; y < compound.getShort("Height"); y++) {
//                    for (int z = 0; z < compound.getShort("Length"); z++) {
//                        int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
//                        BlockPos pos = new BlockPos(x, y, z);
//                        World w = MinecraftServer.getServer().getEntityWorld();
//                        w.setBlockState(pos, Block.getBlockById(blocks[index] & 0xFF).getStateFromMeta(meta[index] & 0xFF), 2);
//                    }
//                }
//            }
//
//
//            DungeonSpecificDataProviderRegistry.doorFinders.put(Pattern.compile("TEST DG"), new DungeonSpecificDataProvider() {
//                @Override
//                public BlockPos findDoor(World w, String dungeonName) {
//                    return new BlockPos(0, 0, 0);
//                }
//
//                @Override
//                public Vector2d findDoorOffset(World w, String dungeonName) {
//                    return null;
//                }
//
//                @Override
//                public BossfightProcessor createBossfightProcessor(World w, String dungeonName) {
//                    return null;
//                }
//
//                @Override
//                public boolean isTrapSpawn(String dungeonName) {
//                    return false;
//                }
//
//                @Override
//                public double secretPercentage(String dungeonName) {
//                    return 0;
//                }
//
//                @Override
//                public int speedSecond(String dungeonName) {
//                    return 0;
//                }
//            });
//            DungeonContext.setDungeonName("TEST DG");
//            DungeonContext fakeContext = new DungeonContext(Minecraft.getMinecraft().theWorld);
//            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(fakeContext);
//            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(true);
//            MapPlayerProcessor mapProcessor = fakeContext.getp();
//            mapProcessor.setUnitRoomDimension(new Dimension(16, 16));
//            mapProcessor.setBugged(false);
//            mapProcessor.setDoorDimensions(new Dimension(4, 4));
//            mapProcessor.setTopLeftMapPoint(new Point(0, 0));
//            fakeContext.setDungeonMin(new BlockPos(0, 70, 0));
//
//            DungeonRoom dungeonRoom = new DungeonRoom(Arrays.asList(new Point(0, 0)), ShortUtils.topLeftifyInt((short) 1), (byte) 63, new BlockPos(0, 70, 0), new BlockPos(31, 70, 31), fakeContext, Collections.emptySet());
//
//            fakeContext.getDungeonRoomList().add(dungeonRoom);
//            for (Point p : Arrays.asList(new Point(0, 0))) {
//                fakeContext.getRoomMapper().put(p, dungeonRoom);
//            }
//
//            EditingContext.createEditingContext(dungeonRoom);
//            EditingContext.getEditingContext().openGui(new GuiDungeonRoomEdit(dungeonRoom));
        } else if ("closecontext".equals(arg)) {
            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(false);

            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
        } else if ("dumpsettings".equals(arg)) {
            NestedCategory nestedCategory = new NestedCategory("ROOT");
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                String category = abstractFeature.getCategory();
                NestedCategory currentRoot = nestedCategory;
                for (String s : category.split("\\.")) {
                    NestedCategory finalCurrentRoot = currentRoot;
                    if (currentRoot.children().containsKey(s))
                        currentRoot = currentRoot.children().get(s);
                    else {
                        currentRoot.child(currentRoot = new NestedCategory(finalCurrentRoot.categoryFull() + "." + s));
                    }
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();

            Stack<Tuple<NestedCategory, Integer>> stak = new Stack<>();
            stak.push(new Tuple<>(nestedCategory, 0));
            Set<NestedCategory> discovered = new HashSet<>();
            while (!stak.isEmpty()) {
                Tuple<NestedCategory, Integer> n = stak.pop();
                if (discovered.contains(n.getFirst())) continue;
                discovered.add(n.getFirst());
                for (Map.Entry<String, NestedCategory> stringNestedCategoryEntry : n.getFirst().children().entrySet()) {
                    stak.push(new Tuple<>(stringNestedCategoryEntry.getValue(), n.getSecond() + 1));
                }

                if (n.getFirst().categoryFull().equals("ROOT")) continue;

                String prefix = "";
                for (int i = 0; i < n.getSecond() - 1; i++) {
                    prefix += "    ";
                }

                List<AbstractFeature> abstractFeatureList = FeatureRegistry.getFeaturesByCategory().getOrDefault(n.getFirst().categoryFull().substring(5), Collections.emptyList());
                stringBuilder.append(prefix).append("- C ").append(n.getFirst().categoryFull()).append("\n");
                stringBuilder2.append(n.getFirst().categoryFull()).append("\n");
                for (AbstractFeature abstractFeature : abstractFeatureList) {
                    stringBuilder.append(prefix).append("    - F ").append(abstractFeature.getName()).append(" / ").append(abstractFeature.getDescription().replace("\n", "$NEW_LINE$")).append("\n");
                }
            }
            System.out.println(stringBuilder.toString());
            System.out.println(stringBuilder2.toString());
        } else if ("readmap".equals(arg)) {
            try {
                int fromX = Integer.parseInt(args[1]);
                int fromY = Integer.parseInt(args[2]);
                sender.addChatMessage(new ChatComponentText(MapUtils.readDigit(MapUtils.getColors(), fromX, fromY) + "-"));
//                int cntY = Integer.parseInt(args[3]);
//                int target = Integer.parseInt(args[4]);
//                StringBuilder sb = new StringBuilder("{");
//                for (int y = fromY; y < fromY + cntY; y++) {
//                    int curr = 0;
//                    for (int x = fromX; x < fromX+8; x++) {
//                        byte clr = MapUtils.getMapColorAt(MapUtils.getColors(), x,y);
//                        if (clr == target) curr = (curr << 1) | 1;
//                        else curr <<= 1;
//                    }
//                    sb.append("0x").append(Integer.toHexString(curr).toUpperCase());
//                    if (y != fromY + cntY - 1) sb.append(", ");
//                }
//                sb.append("}");
//                System.out.println("\n"+sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("testgui".equals(arg)) {
            GuiScreenAdapter adapter = new GuiScreenAdapter(new TestView());
            new Thread(DungeonsGuide.THREAD_GROUP, () -> {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft.getMinecraft().displayGuiScreen(adapter);
                });
            }).start();
        } else if ("clearprofile".equals(arg)) {
            Minecraft.getMinecraft().mcProfiler.clearProfiling();
        } else {
            sender.addChatMessage(new ChatComponentText("ain't gonna find much anything here"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg loadrooms §7-§f Reloads dungeon roomdata."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg brand §7-§f View server brand."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg info §7-§f View Current DG User info."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverun §7-§f Save run to be sent to developer."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverooms §7-§f Saves usergenerated dungeon roomdata."));
        }
    }


    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
