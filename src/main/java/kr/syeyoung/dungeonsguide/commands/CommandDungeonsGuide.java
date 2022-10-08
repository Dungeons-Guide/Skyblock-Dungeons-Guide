/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.commands;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.auth.AuthManager;
import kr.syeyoung.dungeonsguide.auth.authprovider.DgAuth.DgAuthUtil;
import kr.syeyoung.dungeonsguide.party.PartyContext;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.rpc.RichPresenceManager;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfigV2;
import kr.syeyoung.dungeonsguide.config.guiconfig.NestedCategory;
import kr.syeyoung.dungeonsguide.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.MapProcessor;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonEventHolder;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.FeatureViewPlayerStatsOnJoin;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.stomp.*;
import kr.syeyoung.dungeonsguide.utils.AhUtils;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import kr.syeyoung.dungeonsguide.utils.ShortUtils;
import kr.syeyoung.dungeonsguide.wsresource.StaticResourceCache;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class CommandDungeonsGuide extends CommandBase {
    @Override
    public String getCommandName() {
        return "dg";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "dg";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            openConfig = true;
        } else if (args[0].equalsIgnoreCase("saverooms")) {
            DungeonRoomInfoRegistry.saveAll(DungeonsGuide.getDungeonsGuide().getConfigDir());
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved user generated roomdata"));
        } else if (args[0].equalsIgnoreCase("loadrooms")) {
            try {
                DungeonRoomInfoRegistry.loadAll(DungeonsGuide.getDungeonsGuide().getConfigDir());
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully loaded roomdatas"));
                return;
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cAn error has occurred while loading roomdata"));
        } else if (args[0].equalsIgnoreCase("reloadah")) {
            try {
                AhUtils.loadAuctions();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fReloaded Ah data"));
        } else if (args[0].equalsIgnoreCase("brand")) {
            String serverBrand = Minecraft.getMinecraft().thePlayer.getClientBrand();
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e" + serverBrand));
        } else if (args[0].equalsIgnoreCase("reparty")) {
            if (!DungeonsGuide.getDungeonsGuide().getCommandReparty().requestReparty(false)) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cCurrently Repartying"));
            }
        } else if (args[0].equalsIgnoreCase("gui")) {
            openConfig = true;
        } else if (args[0].equalsIgnoreCase("info")) {
            JsonObject obj = DgAuthUtil.getJwtPayload(AuthManager.getInstance().getToken());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fCurrent Plan§7: §e" + obj.get("plan").getAsString()));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fBound to§7: §e" + obj.get("nickname").getAsString()));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fBound uuid§7: §e" + obj.get("uuid").getAsString()));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSession Expire§7: §e" + sdf.format(new Date(obj.get("exp").getAsLong() * 1000))));
        } else if (args[0].equalsIgnoreCase("pathfind")) {
            try {
                DungeonContext context = DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext();
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                if (thePlayer == null) return;
                if (context.getBossfightProcessor() != null) context.getBossfightProcessor().tick();
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
                grp.pathfind("COMMAND", args[1], args[2], FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.getRouteProperties());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (args[0].equals("process") && Minecraft.getMinecraft().getSession().getPlayerID().replace("-", "").equals("e686fe0aab804a71ac7011dc8c2b534c")) {
            File root = DungeonsGuide.getDungeonsGuide().getConfigDir();
            File dir = new File(root, "processorinput");
            File outsecret = new File(root, "processoroutsecret");
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
        } else if (args[0].equals("check") && Minecraft.getMinecraft().getSession().getPlayerID().replace("-", "").equals("e686fe0aab804a71ac7011dc8c2b534c")) {
            File root = DungeonsGuide.getDungeonsGuide().getConfigDir();
            for (File f : root.listFiles()) {
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
        } else if (args[0].equals("reloaddungeon") && Minecraft.getMinecraft().getSession().getPlayerID().replace("-", "").equals("e686fe0aab804a71ac7011dc8c2b534c")) {
            try {
                MinecraftForge.EVENT_BUS.post(new DungeonLeftEvent());
                DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setContext(null);
                MapUtils.clearMap();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (args[0].equalsIgnoreCase("pvall")) {
            PartyManager.INSTANCE.requestPartyList((context) -> {
                if (context == null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cNot in Party"));
                    return;
                }
                FeatureViewPlayerStatsOnJoin.processPartyMembers(context);
            });
//        } else if (args[0].equals("fixschematic")) {
//            File root = new File(e.getDungeonsGuide().getConfigDir(), "schematics");
//            Method method = null;
//            try {
//                method = NBTTagCompound.class.getDeclaredMethod("write", DataOutput.class);
//                method.setAccessible(true);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//                return;
//            }
//            for (File f : root.listFiles()) {
//                try {
//                    NBTTagCompound nbtTagCompound = CompressedStreamTools.readCompressed(new FileInputStream(f));
//                    if (nbtTagCompound.getKeySet().isEmpty()) {
//                        System.out.println("ah");
//                        return;
//                    }
//                    nbtTagCompound.setString("Materials","Alpha");
//                    FileOutputStream fos = new FileOutputStream(f);
//                    DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(fos)));
//
//                    try
//                    {
//                        dataoutputstream.writeByte(nbtTagCompound.getId());
//
//                            dataoutputstream.writeUTF("Schematic");
//                            method.invoke(nbtTagCompound, dataoutputstream);
//                    }
//                    finally
//                    {
//                        dataoutputstream.close();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        } else if (args[0].equalsIgnoreCase("asktojoin") || args[0].equalsIgnoreCase("atj")) {
            if (RichPresenceManager.INSTANCE.getLastSetupCode() == -9999) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cDiscord GameSDK has been disabled, or it failed to load!"));
                return;
            }
            if (!PartyManager.INSTANCE.canInvite()) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cYou don't have perms in the party to invite people!"));
            } else {
                PartyManager.INSTANCE.toggleAllowAskToJoin();
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fToggled Ask to join to " + (PartyManager.INSTANCE.getAskToJoinSecret() != null ? "§eon" : "§coff")));
            }

            if (!FeatureRegistry.DISCORD_RICHPRESENCE.isEnabled()) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cDiscord Rich Presence is disabled! Enable at /dg -> Discord "));
            }
            if (!FeatureRegistry.DISCORD_ASKTOJOIN.isEnabled()) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cDiscord Invite Viewer is disabled! Enable at /dg -> Discord ")); // how
            }
        } else if (args[0].equalsIgnoreCase("partymax") || args[0].equalsIgnoreCase("pm")) {
            if (args.length == 1) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fCurrent party max is §e" + PartyManager.INSTANCE.getMaxParty()));
            } else if (args.length == 2) {
                try {
                    int partyMax = Integer.parseInt(args[1]);
                    if (partyMax < 2) {
                        sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cparty max can't be smaller than 2"));
                        return;
                    }

                    PartyManager.INSTANCE.setMaxParty(partyMax);
                    sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully set partymax to §e" + PartyManager.INSTANCE.getMaxParty()));
                } catch (Exception e) {
                    sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §c" + args[1] + " is not valid number."));
                    return;
                }
            }
        } else if (args[0].equals("partyid")) {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fInternal Party id: " + Optional.ofNullable(PartyManager.INSTANCE.getPartyContext()).map(PartyContext::getPartyID).orElse(null)));
        } else if (args[0].equalsIgnoreCase("loc")) {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fYou're in " + DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName()));
        } else if (args[0].equalsIgnoreCase("saverun")) {
            try {
                File f = DungeonsGuide.getDungeonsGuide().getConfigDir();
                File runDir = new File(f, "dungeonruns");
                runDir.mkdirs();

                File runFile = new File(runDir, UUID.randomUUID() + ".dgrun");

                DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext();
                if (dungeonContext == null) {
                    sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cCouldn't find dungeon to save!"));
                    return;
                }
                DungeonEventHolder dungeonEventHolder = new DungeonEventHolder();
                dungeonEventHolder.setDate(dungeonContext.getInit());
                dungeonEventHolder.setPlayers(dungeonContext.getPlayers());
                dungeonEventHolder.setEventDataList(dungeonContext.getEvents());


                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(runFile));
                oos.writeObject(dungeonEventHolder);
                oos.flush();
                oos.close();
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved dungeon run to " + runFile.getAbsolutePath()));
            } catch (Exception e) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cAn error occured while writing rundata " + e.getMessage()));
                e.printStackTrace();
            }
        } else if (args[0].equals("pv")) {
            try {
                ApiFetcher.fetchUUIDAsync(args[1])
                        .thenAccept(a -> {
                            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e" + args[1] + "§f's Profile ").appendSibling(new ChatComponentText("§7view").setChatStyle(new ChatStyle().setChatHoverEvent(new FeatureViewPlayerStatsOnJoin.HoverEventRenderPlayer(a.orElse(null))))));
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args[0].equals("purge")) {
            ApiFetcher.purgeCache();
            CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
            cosmeticsManager.requestPerms();
            cosmeticsManager.requestCosmeticsList();
            cosmeticsManager.requestActiveCosmetics();
            StaticResourceCache.INSTANCE.purgeCache();
            FeatureRegistry.DISCORD_ASKTOJOIN.imageMap.clear();
            FeatureRegistry.DISCORD_ASKTOJOIN.futureMap.clear();

            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully purged API Cache!"));
        } else if (args[0].equals("pbroadcast")) {
            try {
                String[] payload = new String[args.length - 1];
                System.arraycopy(args, 1, payload, 0, payload.length);
                String actualPayload = String.join(" ", payload).replace("$C$", "§");
                StompManager.getInstance().send(new StompPayload().header("destination", "/app/party.broadcast").payload(
                        new JSONObject().put("partyID", PartyManager.INSTANCE.getPartyContext().getPartyID())
                                .put("payload", actualPayload).toString()
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args[0].equals("requeststaticresource")) {
            UUID uid = UUID.fromString(args[1]);
            StaticResourceCache.INSTANCE.getResource(uid).thenAccept(a -> {
                sender.addChatMessage(new ChatComponentText(a.getResourceID() + ": " + a.getValue() + ": " + a.isExists()));
            });
        } else if (args[0].equals("createFakeRoom") && Minecraft.getMinecraft().getSession().getPlayerID().replace("-", "").equals("e686fe0aab804a71ac7011dc8c2b534c")) {

            // load schematic
            File f = new File(DungeonsGuide.getDungeonsGuide().getConfigDir(), "schematics/new roonm-b2df250c-4af2-4201-963c-0ee1cb6bd3de-5efb1f0c-c05f-4064-bde7-cad0874fdf39.schematic");
            NBTTagCompound compound;
            try {
                compound = CompressedStreamTools.readCompressed(new FileInputStream(f));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            byte[] blocks = compound.getByteArray("Blocks");
            byte[] meta = compound.getByteArray("Data");
            for (int x = 0; x < compound.getShort("Width"); x++) {
                for (int y = 0; y < compound.getShort("Height"); y++) {
                    for (int z = 0; z < compound.getShort("Length"); z++) {
                        int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
                        BlockPos pos = new BlockPos(x, y, z);
                        World w = MinecraftServer.getServer().getEntityWorld();
                        w.setBlockState(pos, Block.getBlockById(blocks[index] & 0xFF).getStateFromMeta(meta[index] & 0xFF), 2);
                    }
                }
            }


            DungeonSpecificDataProviderRegistry.doorFinders.put(Pattern.compile("TEST DG"), new DungeonSpecificDataProvider() {
                @Override
                public BlockPos findDoor(World w, String dungeonName) {
                    return new BlockPos(0, 0, 0);
                }

                @Override
                public Vector2d findDoorOffset(World w, String dungeonName) {
                    return null;
                }

                @Override
                public BossfightProcessor createBossfightProcessor(World w, String dungeonName) {
                    return null;
                }

                @Override
                public boolean isTrapSpawn(String dungeonName) {
                    return false;
                }

                @Override
                public double secretPercentage(String dungeonName) {
                    return 0;
                }

                @Override
                public int speedSecond(String dungeonName) {
                    return 0;
                }
            });
            SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
            skyblockStatus.setDungeonName("TEST DG");
            DungeonContext fakeContext = new DungeonContext(Minecraft.getMinecraft().theWorld);
            skyblockStatus.setContext(fakeContext);
            skyblockStatus.setForceIsOnDungeon(true);
            MapProcessor mapProcessor = fakeContext.getMapProcessor();
            mapProcessor.setUnitRoomDimension(new Dimension(16, 16));
            mapProcessor.setBugged(false);
            mapProcessor.setDoorDimension(new Dimension(4, 4));
            mapProcessor.setTopLeftMapPoint(new Point(0, 0));
            fakeContext.setDungeonMin(new BlockPos(0, 70, 0));

            DungeonRoom dungeonRoom = new DungeonRoom(Arrays.asList(new Point(0, 0)), ShortUtils.topLeftifyInt((short) 1), (byte) 63, new BlockPos(0, 70, 0), new BlockPos(31, 70, 31), fakeContext, Collections.emptySet());

            fakeContext.getDungeonRoomList().add(dungeonRoom);
            for (Point p : Arrays.asList(new Point(0, 0))) {
                fakeContext.getRoomMapper().put(p, dungeonRoom);
            }

            EditingContext.createEditingContext(dungeonRoom);
            EditingContext.getEditingContext().openGui(new GuiDungeonRoomEdit(dungeonRoom));
        } else if (args[0].equals("CloseContext")) {
            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(false);
            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setContext(null);
        } else if (args[0].equals("dumpsettings")) {
            NestedCategory root = new NestedCategory("ROOT");
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                String category = abstractFeature.getCategory();
                NestedCategory currentRoot = root;
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
            stak.push(new Tuple<>(root, 0));
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
        } else if (args[0].equals("readMap")) {
            try {
                int fromX = Integer.parseInt(args[1]);
                int fromY = Integer.parseInt(args[2]);
                sender.addChatMessage(new ChatComponentText(MapUtils.readDigit(MapUtils.getColors(), fromX, fromY)+"-"));
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
        } else {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg §7-§fOpens configuration gui"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg gui §7-§fOpens configuration gui"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg help §7-§fShows command help"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverooms §7-§f Saves usergenerated dungeon roomdata."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg loadrooms §7-§f Reloads dungeon roomdata."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg reloadah §7-§f Reloads price data from server."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg brand §7-§f View server brand."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg reparty §7-§f Reparty."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg info §7-§f View Current DG User info."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg asktojoin or /dg atj §7-§f Toggle ask to join §cRequires Discord Rich Presence enabled. (/dg -> Advanced)"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg partymax [number] or /dg pm [number] §7-§f Sets partymax §7(maximum amount people in party, for discord rpc)"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg pv [ign] §7-§f Profile Viewer"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg pvall §7-§f Profile Viewer For all people on party"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg purge §7-§f Purge api cache."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverun §7-§f Save run to be sent to developer."));
        }
    }

    private boolean openConfig = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        try {
            if (openConfig && e.phase == TickEvent.Phase.START ) {
                openConfig = false;
                Minecraft.getMinecraft().displayGuiScreen(new GuiConfigV2());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
