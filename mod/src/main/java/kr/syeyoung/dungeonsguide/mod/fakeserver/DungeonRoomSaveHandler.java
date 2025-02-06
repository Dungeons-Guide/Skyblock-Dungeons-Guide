package kr.syeyoung.dungeonsguide.mod.fakeserver;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;

public class DungeonRoomSaveHandler implements ISaveHandler, IPlayerFileData {
    public static final WorldSettings WORLD_SETTINGS = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT);
    public static final WorldInfo WORLD_INFO = new WorldInfo(WORLD_SETTINGS, "");
    {
        WORLD_INFO.setDifficulty(EnumDifficulty.PEACEFUL);
        WORLD_INFO.setGameType(WorldSettings.GameType.CREATIVE);

        WORLD_INFO.setBorderSize(32 * 4 + 40);
        WORLD_INFO.getBorderCenterX(32 * 2); // who named these bruh
        WORLD_INFO.getBorderCenterZ(32 * 2);
        WORLD_INFO.setSpawn(new BlockPos(-10, 70, -10));
    }

    private DungeonRoomInfo dungeonRoomInfo;
    public DungeonRoomSaveHandler(DungeonRoomInfo dungeonRoomInfo) {
        this.dungeonRoomInfo = dungeonRoomInfo;
    }


    @Override
    public WorldInfo loadWorldInfo() {
        return WORLD_INFO;
    }

    @Override
    public void checkSessionLock() throws MinecraftException {}

    @Override
    public IChunkLoader getChunkLoader(WorldProvider provider) {
        return new DungeonRoomInfoChunkLoader(dungeonRoomInfo);
    }

    @Override
    public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {

    }

    @Override
    public void saveWorldInfo(WorldInfo worldInformation) {
        // nope.
    }

    @Override
    public IPlayerFileData getPlayerNBTManager() {
        return this;
    }

    @Override
    public void flush() {
        // nope.
    }

    @Override
    public File getWorldDirectory() {
        throw new UnsupportedOperationException("Yikes!");
    }

    @Override
    public File getMapFileFromName(String mapName) {
        return null;
    }

    @Override
    public String getWorldDirectoryName() {
        throw new UnsupportedOperationException("Yikes!");
    }

    @Override
    public void writePlayerData(EntityPlayer player) {

    }

    @Override
    public NBTTagCompound readPlayerData(EntityPlayer player) {
        return null;
    }

    @Override
    public String[] getAvailablePlayerDat() {
        return new String[0];
    }
}
