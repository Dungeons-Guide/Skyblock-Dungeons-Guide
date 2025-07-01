package kr.syeyoung.modapi.v1_8_9.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CustomNetworkPlayerInfoUnloader {

    private static void transform(AbstractClientPlayer abstractClientPlayer) {
        if (abstractClientPlayer == null) return;
        NetworkPlayerInfo uuidNetworkPlayerInfoEntry = ReflectionHelper.getPrivateValue(AbstractClientPlayer.class,
                abstractClientPlayer,
                "playerInfo", "field_175157_a", "a"
        );
        if (uuidNetworkPlayerInfoEntry instanceof CustomNetworkPlayerInfo) {
            S38PacketPlayerListItem s38PacketPlayerListItem = new S38PacketPlayerListItem();
            NetworkPlayerInfo newInfo = new NetworkPlayerInfo(s38PacketPlayerListItem.new AddPlayerData(
                    uuidNetworkPlayerInfoEntry.getGameProfile(),
                    uuidNetworkPlayerInfoEntry.getResponseTime(),
                    uuidNetworkPlayerInfoEntry.getGameType(),
                    ((CustomNetworkPlayerInfo)uuidNetworkPlayerInfoEntry).getOriginalDisplayName()
            ));
            ReflectionHelper.setPrivateValue(AbstractClientPlayer.class,
                    abstractClientPlayer,
                    newInfo,
                    "playerInfo", "field_175157_a", "a"
            );
        }
    }

    public static void unload() {
        try {
            if (Minecraft.getMinecraft().getRenderManager().livingPlayer instanceof AbstractClientPlayer) {
                AbstractClientPlayer ep = (AbstractClientPlayer) Minecraft.getMinecraft().getRenderManager().livingPlayer;
                transform(ep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (Minecraft.getMinecraft().pointedEntity instanceof AbstractClientPlayer) {
                AbstractClientPlayer ep = (AbstractClientPlayer) Minecraft.getMinecraft().pointedEntity;
                transform(ep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetHandlerPlayClient netHandlerPlayClient = Minecraft.getMinecraft().getNetHandler();
        if (netHandlerPlayClient == null && (Minecraft.getMinecraft().getRenderManager().livingPlayer) != null
                && Minecraft.getMinecraft().getRenderManager().livingPlayer instanceof EntityPlayerSP)
            netHandlerPlayClient = ((EntityPlayerSP) Minecraft.getMinecraft().getRenderManager().livingPlayer).sendQueue;

        if (netHandlerPlayClient != null) {
            Map<UUID, NetworkPlayerInfo> playerInfoMap = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class,
                    netHandlerPlayClient, "playerInfoMap", "field_147310_i", "i");
            for (Map.Entry<UUID, NetworkPlayerInfo> uuidNetworkPlayerInfoEntry : playerInfoMap.entrySet()) {
                if (uuidNetworkPlayerInfoEntry.getValue() instanceof CustomNetworkPlayerInfo) {
                    S38PacketPlayerListItem s38PacketPlayerListItem = new S38PacketPlayerListItem();
                    NetworkPlayerInfo newInfo =  new NetworkPlayerInfo(s38PacketPlayerListItem.new AddPlayerData(
                            uuidNetworkPlayerInfoEntry.getValue().getGameProfile(),
                            uuidNetworkPlayerInfoEntry.getValue().getResponseTime(),
                            uuidNetworkPlayerInfoEntry.getValue().getGameType(),
                            ((CustomNetworkPlayerInfo) uuidNetworkPlayerInfoEntry.getValue()).getOriginalDisplayName()
                    ));
                    playerInfoMap.put(uuidNetworkPlayerInfoEntry.getKey(), newInfo);
                }
            }
        }



        World world = Minecraft.getMinecraft().getRenderManager().worldObj;
        if (world != null) {
            for (AbstractClientPlayer entity : world.getEntities(AbstractClientPlayer.class, input -> true)) {
                transform(entity);
            }
            for (AbstractClientPlayer player : world.getPlayers(AbstractClientPlayer.class, input -> true)) {
                transform(player);
            }
            if (world instanceof WorldClient) {
                Set<Entity> list = ReflectionHelper.getPrivateValue(WorldClient.class, (WorldClient) world, "entityList", "field_73032_d", "c");
                for (Entity e : list) {
                    if (e instanceof AbstractClientPlayer) {
                        transform((AbstractClientPlayer) e);
                    }
                }
            }
        }
    }
}
