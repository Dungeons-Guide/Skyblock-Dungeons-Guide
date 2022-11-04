package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.playername;

import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.Replacer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

public class PlayerNameReplacer extends Replacer {
    public PlayerNameReplacer(CosmeticsManager cosmeticsManager) {
        super(cosmeticsManager);
    }

    @Override
    public void consumeEvent(Event e) {
        PlayerEvent.NameFormat nameFormat = (PlayerEvent.NameFormat) e;

        List<ActiveCosmetic> activeCosmetics = cosmeticsManager.getActiveCosmeticByPlayer().get(nameFormat.entityPlayer.getGameProfile().getId());
        if (activeCosmetics == null) return;

        CosmeticData color = null;
        CosmeticData prefix = null;
        for (ActiveCosmetic activeCosmetic : activeCosmetics) {
            CosmeticData cosmeticData = cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData != null){
                if (cosmeticData.getCosmeticType().equals("color")) {
                    color = cosmeticData;
                }
                if (cosmeticData.getCosmeticType().equals("prefix")) {
                    prefix = cosmeticData;
                }
            }
        }


        if (color != null)
            nameFormat.displayname = color.getData().replace("&", "§") + nameFormat.username;

        if (prefix != null)
            nameFormat.displayname = prefix.getData().replace("&", "§") + " " + nameFormat.displayname;


    }


}
