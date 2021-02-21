package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class FeatureCopyMessages extends SimpleFeature implements ChatListener {
    public FeatureCopyMessages() {
        super("ETC", "Copy Chat Messages", "Click on copy to copy", "etc.copymsg");
        setEnabled(false);
    }
    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (!isEnabled()) return;
        if (clientChatReceivedEvent.type == 2) return;

        clientChatReceivedEvent.message.appendSibling(new ChatComponentText("   §7[Copy]").setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtils.stripColor(clientChatReceivedEvent.message.getFormattedText()))).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§eCopy Message")))));
    }
}
