package kr.syeyoung.dungeonsguide.mod.fakeserver;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.modapi.ModAPI;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DungeonServerLaunchUtils {
    public static void launchDungeonServerAndJoin(DungeonRoomInfo dungeonRoomInfo, PathfindPreset preset) {
        if (dungeonRoomInfo.getWorld() == null) throw new IllegalArgumentException("Invalid DRI");
        lastLoadedRoom = dungeonRoomInfo;
        lastLoadedPreset = preset;

        ModAPI.getAPI().getEventBus().fireEvent(new DungeonLeftEvent());
        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);


        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld != null) {
            boolean flag = minecraft.isIntegratedServerRunning();
            boolean flag1 = minecraft.isConnectedToRealms();
            if (theIntegratedServer != null) {
                loadWorld(null, ""); // maybe handle differently
            } else {
                if (!flag)
                    minecraft.theWorld.sendQuittingDisconnectingPacket();
                minecraft.loadWorld( null);
            }
            Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
        }

        doLaunch(dungeonRoomInfo);
    }

    public static void createContext() {
        if (lastLoadedRoom == null) return;
        DungeonRoomInfo dungeonRoomInfo = lastLoadedRoom;
        short shape = dungeonRoomInfo.getShape();
        DungeonContext fakeContext = new DungeonContext("TEST DG", Minecraft.getMinecraft().theWorld, lastLoadedPreset);
        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(fakeContext);
        DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(true);
        DungeonMapLayout dungeonMapLayout = new DungeonMapLayout(
                new Dimension(16, 16),
                5,
                new Point(0,0),
                new BlockPos(0,70,0)
        );
        DungeonRoomScaffoldParser scaffoldParser1 = new DungeonRoomScaffoldParser(dungeonMapLayout, fakeContext);
        fakeContext.setScaffoldParser(scaffoldParser1);

        List<Point> points = new ArrayList<>();

        for (int dy = 0; dy < 4; dy++) {
            for (int dx = 0; dx < 4; dx++) {
                boolean isSet = ((shape>> (dy * 4 + dx)) & 0x1) != 0;
                if (isSet) {
                    points.add(new Point(dx, dy));
                }
            }
        }

        DungeonRoom dungeonRoom = new DungeonRoom(
                Sets.newHashSet(points),
                shape,
                dungeonRoomInfo.getColor(),
                new BlockPos(0, 70, 0),
                new BlockPos(32 * (dungeonRoomInfo.getWidth()/32) - 1, 70, 32 * (dungeonRoomInfo.getLength()/32) - 1),
                fakeContext,
                Collections.emptySet());

        fakeContext.getScaffoldParser().insertRoom(dungeonRoom);
    }
    public static DungeonIntegratedServer theIntegratedServer;

    @Getter
    private static DungeonRoomInfo lastLoadedRoom;
    private static PathfindPreset lastLoadedPreset;

    public static boolean isDungeonIntegratedServerRunning() {
        return theIntegratedServer != null && theIntegratedServer.isServerRunning();
    }

    public static void loadWorld(WorldClient worldClientIn, String loadingMessage) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null) {
            MinecraftForge.EVENT_BUS.post((Event)new WorldEvent.Unload((World)mc.theWorld));
        }
        if (worldClientIn == null) {
            NetHandlerPlayClient nethandlerplayclient = mc.getNetHandler();
            if (nethandlerplayclient != null) {
                nethandlerplayclient.cleanup();
            }
            if (theIntegratedServer != null && theIntegratedServer.isAnvilFileSet()) {
                theIntegratedServer.initiateShutdown();
                theIntegratedServer.setStaticInstance();
                if (mc.loadingScreen != null) {
                    mc.loadingScreen.displayLoadingString(I18n.format("forge.client.shutdown.internal", new Object[0]));
                }
                while (theIntegratedServer != null && !theIntegratedServer.isServerStopped()) {
                    try {
                        Thread.sleep(10L);
                    }
                    catch (InterruptedException interruptedException) {}
                }
            }
            theIntegratedServer = null;
            mc.guiAchievement.clearAchievements();
            mc.entityRenderer.getMapItemRenderer().clearLoadedMaps();
        }
        Minecraft.getMinecraft().setRenderViewEntity(null);
        ReflectionHelper.setPrivateValue(Minecraft.class, mc, null, "myNetworkManager");
        if (mc.loadingScreen != null) {
            mc.loadingScreen.resetProgressAndMessage(loadingMessage);
            mc.loadingScreen.displayLoadingString("");
        }
        if (worldClientIn == null && mc.theWorld != null) {
            mc.getResourcePackRepository().clearResourcePack();
            mc.ingameGUI.resetPlayersOverlayFooterHeader();
            mc.setServerData(null);
            ReflectionHelper.setPrivateValue(Minecraft.class, mc, false, "integratedServerIsRunning");
            FMLClientHandler.instance().handleClientWorldClosing(mc.theWorld);
        }
        mc.getSoundHandler().stopSounds();
        mc.theWorld = worldClientIn;
        {
            mc.getSaveLoader().flushCache();
            mc.thePlayer = null;
        }
        System.gc();
    }
    
    private static void doLaunch(DungeonRoomInfo dungeonRoomInfo) {

        Minecraft mc = Minecraft.getMinecraft();

        mc.loadWorld(null);
        System.gc();

        try {
            theIntegratedServer = new DungeonIntegratedServer(mc, dungeonRoomInfo);
            theIntegratedServer.startServerThread();
            ReflectionHelper.setPrivateValue(Minecraft.class, mc, true, "integratedServerIsRunning", "field_2575", "field_71455_al", "aw");
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
            throw new ReportedException(crashreport);
        }
        mc.loadingScreen.displaySavingString(I18n.format("menu.loadingLevel", new Object[0]));
        while (!theIntegratedServer.serverIsInRunLoop()) {
            if (!StartupQuery.check()) {
                mc.loadWorld(null);
                mc.displayGuiScreen(null);
                return;
            }
            String s = theIntegratedServer.getUserMessage();
            if (s != null) {
                mc.loadingScreen.displayLoadingString(I18n.format(s, new Object[0]));
            } else {
                mc.loadingScreen.displayLoadingString("");
            }
            try {
                Thread.sleep(200L);
            }
            catch (InterruptedException crashreport) {}
        }
        mc.displayGuiScreen(null);
        SocketAddress socketaddress = theIntegratedServer.getNetworkSystem().addLocalEndpoint();
        NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
        networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, mc, null));
        networkmanager.sendPacket(new C00Handshake(47, socketaddress.toString(), 0, EnumConnectionState.LOGIN, true));
        GameProfile gameProfile = mc.getSession().getProfile();
        if (!mc.getSession().hasCachedProperties()) {
            gameProfile = mc.getSessionService().fillProfileProperties(gameProfile, true);
            mc.getSession().setProperties(gameProfile.getProperties());
        }
        networkmanager.sendPacket(new C00PacketLoginStart(gameProfile));

        ReflectionHelper.setPrivateValue(Minecraft.class, mc, networkmanager, "myNetworkManager", "field_2574", "field_71453_ak", "av");
    }
}
