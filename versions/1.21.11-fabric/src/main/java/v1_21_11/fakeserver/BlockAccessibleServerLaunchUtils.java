package v1_21_11.fakeserver;

import kr.syeyoung.modapi.world.IBlockAccessible;

public class BlockAccessibleServerLaunchUtils {
    public static void launchDungeonServerAndJoin(IBlockAccessible dungeonRoomInfo) {
        throw new UnsupportedOperationException("Not yet here yet");
//        Minecraft minecraft = Minecraft.getMinecraft();
//        if (minecraft.theWorld != null) {
//            boolean flag = minecraft.isIntegratedServerRunning();
//            boolean flag1 = minecraft.isConnectedToRealms();
//            if (theIntegratedServer != null) {
//                loadWorld(null, ""); // maybe handle differently
//            } else {
//                if (!flag)
//                    minecraft.theWorld.sendQuittingDisconnectingPacket();
//                minecraft.loadWorld( null);
//            }
//            Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
//        }
//
//        doLaunch(dungeonRoomInfo);
    }

//    public static BlockAccessibleIntegratedServer theIntegratedServer;

//    public static boolean isDungeonIntegratedServerRunning() {
//        return theIntegratedServer != null && theIntegratedServer.isServerRunning();
//    }
//
//    public static void loadWorld(WorldClient worldClientIn, String loadingMessage) {
//        Minecraft mc = Minecraft.getMinecraft();
//        if (mc.theWorld != null) {
//            MinecraftForge.EVENT_BUS.post((Event)new WorldEvent.Unload((World)mc.theWorld));
//        }
//        if (worldClientIn == null) {
//            NetHandlerPlayClient nethandlerplayclient = mc.getNetHandler();
//            if (nethandlerplayclient != null) {
//                nethandlerplayclient.cleanup();
//            }
//            if (theIntegratedServer != null && theIntegratedServer.isAnvilFileSet()) {
//                theIntegratedServer.initiateShutdown();
//                theIntegratedServer.setStaticInstance();
//                if (mc.loadingScreen != null) {
//                    mc.loadingScreen.displayLoadingString(I18n.format("forge.client.shutdown.internal", new Object[0]));
//                }
//                while (theIntegratedServer != null && !theIntegratedServer.isServerStopped()) {
//                    try {
//                        Thread.sleep(10L);
//                    }
//                    catch (InterruptedException interruptedException) {}
//                }
//            }
//            theIntegratedServer = null;
//            mc.guiAchievement.clearAchievements();
//            mc.entityRenderer.getMapItemRenderer().clearLoadedMaps();
//        }
//        Minecraft.getMinecraft().setRenderViewEntity(null);
//        ReflectionHelper.setPrivateValue(Minecraft.class, mc, null, "myNetworkManager");
//        if (mc.loadingScreen != null) {
//            mc.loadingScreen.resetProgressAndMessage(loadingMessage);
//            mc.loadingScreen.displayLoadingString("");
//        }
//        if (worldClientIn == null && mc.theWorld != null) {
//            mc.getResourcePackRepository().clearResourcePack();
//            mc.ingameGUI.resetPlayersOverlayFooterHeader();
//            mc.setServerData(null);
//            ReflectionHelper.setPrivateValue(Minecraft.class, mc, false, "integratedServerIsRunning");
//            FMLClientHandler.instance().handleClientWorldClosing(mc.theWorld);
//        }
//        mc.getSoundHandler().stopSounds();
//        mc.theWorld = worldClientIn;
//        {
//            mc.getSaveLoader().flushCache();
//            mc.thePlayer = null;
//        }
//        System.gc();
//    }
//
//    private static void doLaunch(IBlockAccessible dungeonRoomInfo) {
//
//        Minecraft mc = Minecraft.getMinecraft();
//
//        mc.loadWorld(null);
//        System.gc();
//
//        try {
//            theIntegratedServer = new BlockAccessibleIntegratedServer(mc, dungeonRoomInfo);
//            theIntegratedServer.startServerThread();
//            ReflectionHelper.setPrivateValue(Minecraft.class, mc, true, "integratedServerIsRunning", "field_2575", "field_71455_al", "aw");
//        }
//        catch (Throwable throwable) {
//            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
//            CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
//            throw new ReportedException(crashreport);
//        }
//        mc.loadingScreen.displaySavingString(I18n.format("menu.loadingLevel", new Object[0]));
//        while (!theIntegratedServer.serverIsInRunLoop()) {
//            if (!StartupQuery.check()) {
//                mc.loadWorld(null);
//                mc.displayGuiScreen(null);
//                return;
//            }
//            String s = theIntegratedServer.getUserMessage();
//            if (s != null) {
//                mc.loadingScreen.displayLoadingString(I18n.format(s, new Object[0]));
//            } else {
//                mc.loadingScreen.displayLoadingString("");
//            }
//            try {
//                Thread.sleep(200L);
//            }
//            catch (InterruptedException crashreport) {}
//        }
//        mc.displayGuiScreen(null);
//        SocketAddress socketaddress = theIntegratedServer.getNetworkSystem().addLocalEndpoint();
//        NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
//        networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, mc, null));
//        networkmanager.sendPacket(new C00Handshake(47, socketaddress.toString(), 0, EnumConnectionState.LOGIN, true));
//        GameProfile gameProfile = mc.getSession().getProfile();
//        if (!mc.getSession().hasCachedProperties()) {
//            gameProfile = mc.getSessionService().fillProfileProperties(gameProfile, true);
//            mc.getSession().setProperties(gameProfile.getProperties());
//        }
//        networkmanager.sendPacket(new C00PacketLoginStart(gameProfile));
//
//        ReflectionHelper.setPrivateValue(Minecraft.class, mc, networkmanager, "myNetworkManager", "field_2574", "field_71453_ak", "av");
//    }
}
