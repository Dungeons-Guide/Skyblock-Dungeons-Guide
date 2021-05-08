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
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonEventHolder;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.events.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.FeatureViewPlayerOnJoin;
import kr.syeyoung.dungeonsguide.features.impl.party.api.ApiFetchur;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.stomp.*;
import kr.syeyoung.dungeonsguide.utils.AhUtils;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

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
            DungeonsGuide.getDungeonsGuide().getCommandReparty().requestReparty();
        } else if (args[0].equalsIgnoreCase("gui")) {
            openConfig = true;
        } else if (args[0].equalsIgnoreCase("info")) {
            JsonObject obj = DungeonsGuide.getDungeonsGuide().getAuthenticator().getJwtPayload(DungeonsGuide.getDungeonsGuide().getAuthenticator().getToken());
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
                grp.pathfind(args[1], args[2]);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (args[0].equals("process") && Minecraft.getMinecraft().getSession().getPlayerID().replace("-", "").equals("e686fe0aab804a71ac7011dc8c2b534c")) {
            File root = DungeonsGuide.getDungeonsGuide().getConfigDir();
            File dir = new File(root, "processorinput");
            File outpuzzle = new File(root, "processoroutpuzzle");
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

                    dri.getMechanics().clear();

                    fos = new FileOutputStream(new File(outpuzzle, dri.getUuid().toString() + ".roomdata"));
                    oos = new ObjectOutputStream(fos);
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
                    System.out.println("Starting at "+dri.getName() +" - "+dri.getUuid());
                    for (Map.Entry<String, DungeonMechanic> value2 : dri.getMechanics().entrySet()) {
                        DungeonMechanic value = value2.getValue();
                        if (value instanceof DungeonSecret &&
                                (((DungeonSecret) value).getSecretType() == DungeonSecret.SecretType.BAT
                        || ((DungeonSecret) value).getSecretType() == DungeonSecret.SecretType.CHEST)
                        && ((DungeonSecret) value).getSecretPoint().getY() == 0) {
                            OffsetPoint offsetPoint = ((DungeonSecret) value).getSecretPoint();
                            dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                            System.out.println("Fixing "+value2.getKey()+" - as secret "+((DungeonSecret) value).getSecretType() + " - at "+((DungeonSecret) value).getSecretPoint());
                        } else if (value instanceof DungeonOnewayDoor) {
                            for (OffsetPoint offsetPoint : ((DungeonOnewayDoor) value).getSecretPoint().getOffsetPointList()) {
                                if (offsetPoint.getY() == 0) {
                                    dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                    System.out.println("Fixing "+value2.getKey()+" - o-door - at "+offsetPoint);
                                }
                            }
                        } else if (value instanceof DungeonDoor) {
                            for (OffsetPoint offsetPoint : ((DungeonDoor) value).getSecretPoint().getOffsetPointList()) {
                                if (offsetPoint.getY() == 0) {
                                    dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                    System.out.println("Fixing "+value2.getKey()+" - door - at "+offsetPoint);
                                }
                            }
                        } else if (value instanceof DungeonBreakableWall) {
                            for (OffsetPoint offsetPoint : ((DungeonBreakableWall) value).getSecretPoint().getOffsetPointList()) {
                                if (offsetPoint.getY() == 0) {
                                    dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                    System.out.println("Fixing "+value2.getKey()+" - wall - at "+offsetPoint);
                                }
                            }
                        } else if (value instanceof DungeonTomb) {
                            for (OffsetPoint offsetPoint : ((DungeonTomb) value).getSecretPoint().getOffsetPointList()) {
                                if (offsetPoint.getY() == 0) {
                                    dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                    System.out.println("Fixing "+value2.getKey()+" - crypt - at "+offsetPoint);
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
        } else if (args[0].equals("reloaddungeon") && Minecraft.getMinecraft().getSession().getPlayerID().replace("-", "").equals("e686fe0aab804a71ac7011dc8c2b534c")){
            try {
                MinecraftForge.EVENT_BUS.post(new DungeonLeftEvent());
                DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setContext(null);
                MapUtils.clearMap();
            } catch (Throwable t) {
                t.printStackTrace();
            }
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
            if (!PartyManager.INSTANCE.isCanInvite()) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cYou don't have perms in the party to invite people!"));
            } else {
                PartyManager.INSTANCE.toggleAllowAskToJoin();
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fToggled Ask to join to "+(PartyManager.INSTANCE.isAllowAskToJoin() ? "§eon" : "§coff")));
            }

            if (!FeatureRegistry.ADVANCED_RICHPRESENCE.isEnabled()) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cDiscord Rich Presence is disabled! Enable at /dg -> Advanced "));
            }
        } else if (args[0].equalsIgnoreCase("partymax") || args[0].equalsIgnoreCase("pm")) {
            if (args.length == 1){
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fCurrent party max is §e"+PartyManager.INSTANCE.getMaxParty()));
            } else if (args.length == 2) {
                try {
                    int partyMax = Integer.parseInt(args[1]);
                    if (partyMax < 2) {
                        sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cparty max can't be smaller than 2"));
                        return;
                    }

                    PartyManager.INSTANCE.setMaxParty(partyMax);
                    sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully set partymax to §e"+PartyManager.INSTANCE.getMaxParty()));
                } catch (Exception e) {
                    sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §c"+args[1]+" is not valid number."));
                    return;
                }
            }
        } else if (args[0].equals("partyid")) {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fInternal Party id: "+PartyManager.INSTANCE.getPartyID()));
        } else if (args[0].equalsIgnoreCase("loc")) {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fYou're in "+ DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName()));
        } else if (args[0].equalsIgnoreCase("saverun")) {
            try {
                File f = DungeonsGuide.getDungeonsGuide().getConfigDir();
                File runDir = new File(f, "dungeonruns");
                runDir.mkdirs();

                File runFile = new File(runDir, UUID.randomUUID() +".dgrun");

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
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved dungeon run to "+runFile.getAbsolutePath()));
            } catch (Exception e) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cAn error occured while writing rundata "+e.getMessage()));
                e.printStackTrace();
            }
        } else if (args[0].equals("pv")) {
            try {
                ApiFetchur.fetchUUIDAsync(args[1])
                        .thenAccept(a -> {
                            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e"+args[1]+"§f's Profile ").appendSibling(new ChatComponentText("§7view").setChatStyle(new ChatStyle().setChatHoverEvent(new FeatureViewPlayerOnJoin.HoverEventRenderPlayer(a.orElse(null))))));
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args[0].equals("purge")) {
            ApiFetchur.purgeCache();
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully purged API Cache!"));
        } else if (args[0].equals("send")) {
            DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload().header("destination", args[1]).method(StompHeader.SEND).payload(args[2]));
        } else if (args[0].equals("subscribe")) {
            DungeonsGuide.getDungeonsGuide().getStompConnection().subscribe(StompSubscription.builder().destination(args[1]).ackMode(StompSubscription.AckMode.AUTO).stompMessageHandler(new StompMessageHandler() {
                @Override
                public void handle(StompInterface stompInterface, StompPayload stompPayload) {

                }
            }).build());
        } else if (args[0].equals("echo")) {
            for (NetworkPlayerInfo networkPlayerInfo : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
                System.out.println(networkPlayerInfo);
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
        }
    }

    private boolean openConfig = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        try {
            if (openConfig && e.phase == TickEvent.Phase.START ) {
                openConfig = false;
                Minecraft.getMinecraft().displayGuiScreen(new GuiConfig());
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
