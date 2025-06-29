package kr.syeyoung.modapi.v1_8_9.fakeserver;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.stats.StatisticsFile;

import java.net.SocketAddress;

public class BlockAccessibleServerConfigManager extends ServerConfigurationManager {
    public BlockAccessibleServerConfigManager(BlockAccessibleIntegratedServer server) {
        super(server);
        this.setViewDistance(10);
        this.setCommandsAllowedForAll(true);
    }

    private NBTTagCompound hostPlayerData;

    protected void writePlayerData(EntityPlayerMP playerIn) {
        if (playerIn.getName().equals(this.getServerInstance().getServerOwner())) {
            this.hostPlayerData = new NBTTagCompound();
            playerIn.writeToNBT(this.hostPlayerData);
        }

        super.writePlayerData(playerIn);
    }

    public String allowUserToConnect(SocketAddress address, GameProfile profile) {
        return null;
    }

    public BlockAccessibleIntegratedServer getServerInstance() {
        return (BlockAccessibleIntegratedServer)super.getServerInstance();
    }

    @Override
    public StatisticsFile getPlayerStatsFile(EntityPlayer playerIn) {
        StatisticsFile statisticsfile = new StatisticsFile(this.getServerInstance(), null);
        return statisticsfile;
    }

    public NBTTagCompound getPlayerNBT(EntityPlayerMP player) {
        NBTTagCompound nbttagcompound = this.getServerInstance().worldServers[0].getWorldInfo().getPlayerNBTTagCompound();
        if (player.getName().equals(this.getServerInstance().getServerOwner()) && nbttagcompound != null) {
            return nbttagcompound;
        }
        return this.getServerInstance().worldServers[0].getSaveHandler().getPlayerNBTManager().readPlayerData(player);
    }

    public NBTTagCompound getHostPlayerData() {
        return this.hostPlayerData;
    }
}
