package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat;

import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.List;

public class ReplacerUtil {

    public static boolean isNotNullAndDoesStartWith(IChatComponent e, String startsWith){
        if(!messageNullCheck(e)) return false;

        return startsWith(e, startsWith);
    }

    public static boolean startsWith(IChatComponent event, String start){
        if(event == null ) return false;
        if(event.getChatStyle() == null) return false;
        if(event.getChatStyle().getChatClickEvent() == null) return false;
        if(event.getChatStyle().getChatClickEvent().getValue() == null) return false;

        return event.getChatStyle().getChatClickEvent().getValue().startsWith(start);
    }

    public static boolean messageNullCheck(IChatComponent message){
        return message.getChatStyle() != null && message.getChatStyle().getChatClickEvent() != null;
    }

    public static void chatsomethig(ClientChatReceivedEvent event, CosmeticsManager cosmeticsManager) {
        String username = event.message.getChatStyle().getChatClickEvent().getValue().split(" ")[1];
        List<ActiveCosmetic> cDatas = cosmeticsManager.getActiveCosmeticByPlayerNameLowerCase().get(username.toLowerCase());

        if (null == cDatas) {
            return;
        }

        CosmeticData color = null;
        CosmeticData prefix = null;
        for (ActiveCosmetic activeCosmetic : cDatas) {
            CosmeticData cosmeticData = cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData != null && cosmeticData.getCosmeticType().equals("color")) {
                color = cosmeticData;
            } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("prefix")) {
                prefix = cosmeticData;
            }
        }

        String[] splitInto = event.message.getUnformattedTextForChat().split(" ");
        int lastValidNickname = -1;
        int lastprefix = -1;
        for (int i = 0; i < splitInto.length; i++) {
            String s = splitInto[i];
            if (s.startsWith("§7")) s = s.substring(2);
            char c = s.charAt(0);
            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == '_' || c == '-') {
                lastValidNickname = i;
                if (i >= 1) {
                    String str = TextUtils.stripColor(splitInto[i - 1]);
                    if (str.startsWith("[") && str.endsWith("]")) break;
                }
            }
        }
        if (lastValidNickname == -1) return;

        if (lastValidNickname - 1 >= 0 && TextUtils.stripColor(splitInto[lastValidNickname - 1]).charAt(0) == '[')
            lastprefix = lastValidNickname - 1;
        else lastprefix = lastValidNickname;

        String building = "";
        for (int i = 0; i < lastprefix; i++) {
            building += splitInto[i] + " ";
        }
        if (prefix != null) building += prefix.getData().replace("&", "§") + " ";
        for (int i = lastprefix; i < lastValidNickname; i++) {
            building += splitInto[i] + " ";
        }
        if (color != null) {
            String nick = splitInto[lastValidNickname];
            building += color.getData().replace("&", "§");
            boolean foundLegitChar = false;
            boolean foundColor = false;
            for (char c : nick.toCharArray()) {
                if (foundColor) {
                    foundColor = false;
                    continue;
                }
                if (c == '§' && !foundLegitChar) foundColor = true;
                else {
                    foundLegitChar = true;
                    building += c;
                }
            }
            building += " ";
        } else {
            building += splitInto[lastValidNickname] + " ";
        }
        for (int i = lastValidNickname + 1; i < splitInto.length; i++) {
            building += splitInto[i] + " ";
        }
        if (event.message.getUnformattedTextForChat().charAt(event.message.getUnformattedTextForChat().length() - 1) != ' ')
            building = building.substring(0, building.length() - 1);

        ChatComponentText newChatCompText = new ChatComponentText(building);
        newChatCompText.setChatStyle(event.message.getChatStyle());
        newChatCompText.getSiblings().addAll(event.message.getSiblings());

        event.message = newChatCompText;
    }
}
