package kr.syeyoung.dungeonsguide.commands;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.utils.AhUtils;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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
            DungeonRoomInfoRegistry.saveAll(e.getDungeonsGuide().getConfigDir());
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved user generated roomdata"));
        } else if (args[0].equalsIgnoreCase("loadrooms")) {
            try {
                DungeonRoomInfoRegistry.loadAll(e.getDungeonsGuide().getConfigDir());
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
            e.getDungeonsGuide().getCommandReparty().requestReparty();
        } else if (args[0].equalsIgnoreCase("gui")) {
            openConfig = true;
        } else if (args[0].equalsIgnoreCase("info")) {
            JsonObject obj = e.getDungeonsGuide().getAuthenticator().a(e.getDungeonsGuide().getAuthenticator().c());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fCurrent Plan§7: §e" + obj.get("plan").getAsString()));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fBound to§7: §e" + obj.get("nickname").getAsString()));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fBound uuid§7: §e" + obj.get("uuid").getAsString()));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSession Expire§7: §e" + sdf.format(new Date(obj.get("exp").getAsLong() * 1000))));
        } else if (args[0].equalsIgnoreCase("pathfind")) {
            try {
                DungeonContext context = e.getDungeonsGuide().getSkyblockStatus().getContext();
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
            File root = e.getDungeonsGuide().getConfigDir();
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
            File root = e.getDungeonsGuide().getConfigDir();
            for (File f : root.listFiles()) {
                if (!f.getName().endsWith(".roomdata")) continue;
                try {
                    InputStream fis = new FileInputStream(f);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    DungeonRoomInfo dri = (DungeonRoomInfo) ois.readObject();
                    ois.close();
                    fis.close();
                    for (Map.Entry<String, DungeonMechanic> stringDungeonMechanicEntry : dri.getMechanics().entrySet()) {
                        if (stringDungeonMechanicEntry.getValue() instanceof DungeonSecret) {
                            if (stringDungeonMechanicEntry.getKey().charAt(0) != Character.toLowerCase(((DungeonSecret) stringDungeonMechanicEntry.getValue()).getSecretType().name().charAt(0))) {
                                System.out.println("Mismatch found at "+dri.getName() + " - "+stringDungeonMechanicEntry.getKey() +" - "+((DungeonSecret) stringDungeonMechanicEntry.getValue()).getSecretType());
                                System.out.print(' ');
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
                e.getDungeonsGuide().getSkyblockStatus().setContext(null);
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
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fYou're in "+e.getDungeonsGuide().getSkyblockStatus().getDungeonName()));
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
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg asktojoin or /dg atj §7-§f Toggle ask to join"));
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
