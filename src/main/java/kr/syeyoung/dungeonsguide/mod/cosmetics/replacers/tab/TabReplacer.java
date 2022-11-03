package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.tab;

import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.Replacer;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerListItemPacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.Map;
import java.util.UUID;

public class TabReplacer extends Replacer {
    public TabReplacer(CosmeticsManager cosmeticsManager) {
        super(cosmeticsManager);
    }

    @Override
    public void consumeEvent(Event e) {
        PlayerListItemPacketEvent packetPlayerListItem = (PlayerListItemPacketEvent) e;

        S38PacketPlayerListItem asd = packetPlayerListItem.getPacketPlayerListItem();
        if (asd.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
            if (Minecraft.getMinecraft().getNetHandler() == null) return;

            Map<UUID, NetworkPlayerInfo> playerInfoMap = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, Minecraft.getMinecraft().getNetHandler(), "playerInfoMap", "field_147310_i","i");
            for (S38PacketPlayerListItem.AddPlayerData entry : asd.getEntries()) {
                playerInfoMap.remove(entry.getProfile().getId());
                playerInfoMap.put(entry.getProfile().getId(), new CustomNetworkPlayerInfo(entry));
            }
        }
    }
}
