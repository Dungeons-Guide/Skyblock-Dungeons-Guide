package kr.syeyoung.dungeonsguide.mod.fakeserver;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedPlayerList;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.integrated.IntegratedServerCommandManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.world.*;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class DungeonIntegratedServer extends MinecraftServer {
    private static final Logger logger = LogManager.getLogger();
    /**
     * The Minecraft instance.
     */
    private final Minecraft mc;

    private DungeonRoomInfo dungeonRoomInfo;

    public DungeonIntegratedServer(Minecraft mcIn, DungeonRoomInfo dungeonRoomInfo) {
        super(new File(mcIn.mcDataDir, "saves"), mcIn.getProxy(), new File(mcIn.mcDataDir, USER_CACHE_FILE.getName()));
        this.setServerOwner(mcIn.getSession().getUsername());
        this.setWorldName(dungeonRoomInfo.getName());
        this.setDemo(mcIn.isDemo());
        this.canCreateBonusChest(false);
        this.setBuildLimit(256);
        this.setConfigManager(new DungeonServerConfigManager(this));
        this.mc = mcIn;
        this.dungeonRoomInfo = dungeonRoomInfo;
    }

    @Override
    public boolean isSinglePlayer() {
        return true;
    }

    @Override
    protected ServerCommandManager createNewCommandManager() {
        return new IntegratedServerCommandManager();
    }

    @Override
    protected void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String worldNameIn2) {
        throw new UnsupportedOperationException("Yikes");
    }

    protected void loadWorlds() {
        WorldServer overWorld =(WorldServer) new DungeonWorldServer(this, new DungeonRoomSaveHandler(dungeonRoomInfo), DungeonRoomSaveHandler.WORLD_INFO, 0, this.theProfiler).init();
        overWorld.initialize(DungeonRoomSaveHandler.WORLD_SETTINGS);
        overWorld.setSpawnPoint(new BlockPos(0, 120, 0));
        overWorld.addWorldAccess(new WorldManager(this, overWorld));
        if (!this.isSinglePlayer()) {
            overWorld.getWorldInfo().setGameType(this.getGameType());
        }
        MinecraftForge.EVENT_BUS.post(new WorldEvent(overWorld));

        this.getConfigurationManager().setPlayerManager(new WorldServer[]{overWorld});

        this.initialWorldChunkLoad();
    }

    @Override
    protected boolean startServer() throws IOException {
        logger.info("Starting integrated minecraft server version 1.8.9");
        this.setOnlineMode(true);
        this.setCanSpawnAnimals(false);
        this.setCanSpawnNPCs(false);
        this.setAllowPvp(false);
        this.setAllowFlight(true);
        logger.info("Generating keypair");
        this.setKeyPair(CryptManager.generateKeyPair());
        if (!FMLCommonHandler.instance().handleServerAboutToStart(this)) {
            return false;
        }

        this.loadWorlds();

        this.setMOTD(this.getServerOwner() + " - " + this.worldServers[0].getWorldInfo().getWorldName());
        return FMLCommonHandler.instance().handleServerStarting(this);
    }

    @Override
    public String getFolderName() {
        return null;
    }

    @Override
    public void setFolderName(String name) {
        throw new UnsupportedOperationException("Yikes");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void tick() {
        boolean isGamePaused = Minecraft.getMinecraft().getNetHandler() != null && Minecraft.getMinecraft().isGamePaused();
        if (isGamePaused) {
            synchronized (this.futureTaskQueue) {
                while (!this.futureTaskQueue.isEmpty()) {
                    Util.runTask((FutureTask)this.futureTaskQueue.poll(), logger);
                }
            }
        } else {
            super.tick();
            if (this.mc.gameSettings.renderDistanceChunks != this.getConfigurationManager().getViewDistance()) {
                logger.info("Changing view distance to {}, from {}", this.mc.gameSettings.renderDistanceChunks, this.getConfigurationManager().getViewDistance());
                this.getConfigurationManager().setViewDistance(this.mc.gameSettings.renderDistanceChunks);
            }
        }
    }

    @Override
    protected void saveAllWorlds(boolean dontLog) {
        // don't do anything.
        return;
    }

    @Override
    public boolean canStructuresSpawn() {
        return false;
    }

    @Override
    public WorldSettings.GameType getGameType() {
        return WorldSettings.GameType.CREATIVE;
    }

    @Override
    public EnumDifficulty getDifficulty() {
        return EnumDifficulty.PEACEFUL;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public boolean shouldBroadcastRconToOps() {
        return true;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return true;
    }

    @Override
    public File getDataDirectory() {
        return this.mc.mcDataDir; // hmmm
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public boolean shouldUseNativeTransport() {
        return false;
    }

    @Override
    protected void finalTick(CrashReport report) {
        this.mc.crashed(report);
    }

    @Override
    public CrashReport addServerInfoToCrashReport(CrashReport report) {
        report = super.addServerInfoToCrashReport(report);
        report.getCategory().addCrashSectionCallable("Type", new Callable<String>(){

            @Override
            public String call() throws Exception {
                return "Dungeon Integrated Server (map_client.txt)";
            }
        });
        report.getCategory().addCrashSectionCallable("Is Modded", new Callable<String>(){

            @Override
            public String call() throws Exception {
                String s = ClientBrandRetriever.getClientModName();
                if (!s.equals("vanilla")) {
                    return "Definitely; Client brand changed to '" + s + "'";
                }
                s = DungeonIntegratedServer.this.getServerModName();
                return !s.equals("vanilla") ? "Definitely; Server brand changed to '" + s + "'" : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.");
            }
        });
        return report;
    }

    @Override
    public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
        throw new UnsupportedOperationException("Yikes");
    }


    @Override
    public boolean isSnooperEnabled() {
        return false;
    }

    @Override
    public String shareToLAN(WorldSettings.GameType type, boolean allowCheats) {
        throw new UnsupportedOperationException("Yikes");
    }


    @Override
    protected void systemExitNow() {
        DungeonServerLaunchUtils.theIntegratedServer = null;
    }

    @Override
    public void initiateShutdown() {
        Futures.getUnchecked(this.addScheduledTask(new Runnable(){
            @Override
            public void run() {
                for (EntityPlayerMP entityplayermp : Lists.newArrayList(DungeonIntegratedServer.this.getConfigurationManager().getPlayerList())) {
                    DungeonIntegratedServer.this.getConfigurationManager().playerLoggedOut(entityplayermp);
                }
            }
        }));
        super.initiateShutdown();
    }

    public void setStaticInstance() {
        this.setInstance();
    }

    /**
     * Returns true if this integrated server is open to LAN
     */
    public boolean getPublic() {
        return false;
    }

    @Override
    public void setGameType(WorldSettings.GameType gameMode) {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return false;
    }

    @Override
    public int getOpPermissionLevel() {
        return 4;
    }
}
