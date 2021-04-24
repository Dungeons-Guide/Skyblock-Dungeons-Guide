package kr.syeyoung.dungeonsguide.features.impl.party;

import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.ChatListenerGlobal;
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
        // ay set apikey
    }
}
