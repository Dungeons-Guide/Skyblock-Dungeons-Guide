package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat;

import net.minecraft.util.IChatComponent;

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

}
