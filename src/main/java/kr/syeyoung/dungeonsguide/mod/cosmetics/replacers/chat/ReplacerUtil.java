package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat;

import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.CosmeticData;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReplacerUtil {

    public static boolean isNotNullAndDoesStartWith(IChatComponent e, String startsWith){
        if (e.getChatStyle() != null && e.getChatStyle().getChatClickEvent() != null) {
            return startsWith(e, startsWith);
        }
        return false;

    }

    public static boolean startsWith(IChatComponent event, String start){
        if(event == null ) return false;
        if(event.getChatStyle() == null) return false;
        if(event.getChatStyle().getChatClickEvent() == null) return false;
        if(event.getChatStyle().getChatClickEvent().getValue() == null) return false;

        return event.getChatStyle().getChatClickEvent().getValue().startsWith(start);
    }

    @Nullable
    public static CosmeticData getPrefixCosmeticData(@Nullable List<ActiveCosmetic> activeCosmeticsList, @NotNull CosmeticsManager cosmeticsManager) {
        if (activeCosmeticsList == null) return null;

        for (ActiveCosmetic activeCosmetic : activeCosmeticsList) {
            CosmeticData cosmeticData = cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData != null && cosmeticData.getCosmeticType().equals("prefix")) {
                return cosmeticData;
            }
        }
        return null;
    }

    @Nullable
    public static CosmeticData getColorCosmeticData(@Nullable List<ActiveCosmetic> activeCosmeticsList, @NotNull CosmeticsManager cosmeticsManager) {
        if (activeCosmeticsList == null) return null;

        for (ActiveCosmetic activeCosmetic : activeCosmeticsList) {
            CosmeticData cosmeticData = cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData != null && cosmeticData.getCosmeticType().equals("color")) {
                return cosmeticData;
            }
        }
        return null;
    }

    public static List<ActiveCosmetic> getActiveCosmeticsFromUsername(String username, CosmeticsManager cosmeticsManager) {
        username = username.toLowerCase();
        return cosmeticsManager.getActiveCosmeticByPlayerNameLowerCase().get(username);
    }
}
