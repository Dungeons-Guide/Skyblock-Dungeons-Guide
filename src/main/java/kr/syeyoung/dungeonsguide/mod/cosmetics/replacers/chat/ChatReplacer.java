package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat;

import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.Replacer;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat.impl.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatReplacer extends Replacer {
    private final List<IChatReplacer> iChatReplacers;

    public ChatReplacer(@NotNull CosmeticsManager m) {
        super(m);

        this.iChatReplacers = new ArrayList<>();

        // dg's "click to view profile"
        iChatReplacers.add(new LobbyJoinMessages());

        // global chat
        iChatReplacers.add(new PublicChat());

        // coop
        iChatReplacers.add(new CoopChat());

        // private message
        iChatReplacers.add(new PrivateMessage());

        // chat that you typed
        iChatReplacers.add(new Chat());
    }

    @Override
    public void consumeEvent(Event e) {
        ClientChatReceivedEvent chatEvent = (ClientChatReceivedEvent) e;
        if (chatEvent.type == 2) return;
        try {
            for (IChatReplacer iChatReplacer : iChatReplacers) {
                if (iChatReplacer.isApplyable(chatEvent)) {
                    iChatReplacer.transformIntoCosmeticsForm(chatEvent, cosmeticsManager);
                    return;
                }
            }
        } catch (Throwable t) {
            System.out.println(chatEvent.message);
            t.printStackTrace();
        }
    }


}
