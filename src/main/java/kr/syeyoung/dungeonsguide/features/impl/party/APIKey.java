package kr.syeyoung.dungeonsguide.features.impl.party;

import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.ChatListenerGlobal;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class APIKey extends SimpleFeature implements ChatListenerGlobal {

    public APIKey() {
        super("Party Kicker", "API KEY", "Set api key. Disabling this feature does nothing","partykicker.apikey");
        parameters.put("apikey", new FeatureParameter<String>("apikey", "API Key", "API key", "","string"));
    }

    public String getAPIKey() {
        return this.<String>getParameter("apikey").getValue();
    }


    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.type == 2) return;
        String str = clientChatReceivedEvent.message.getFormattedText();
        if (str.startsWith("§aYour new API key is §r§b")) {
            String apiKeys = TextUtils.stripColor(str.split(" ")[5]);
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fAutomatically Configured Hypixel API Key"));
            this.<String>getParameter("apikey").setValue(apiKeys);
        }

    }
}
