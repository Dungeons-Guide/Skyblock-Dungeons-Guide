/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.party;

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListenerGlobal;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class APIKey extends SimpleFeature implements ChatListenerGlobal {

    public APIKey() {
        super("Misc.API Features", "API KEY", "Sets api key","partykicker.apikey");
        addParameter("apikey", new FeatureParameter<String>("apikey", "API Key", "API key", "","string"));
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
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fAutomatically Configured Hypixel API Key"));
            this.<String>getParameter("apikey").setValue(apiKeys);
        }
    }

    @Override
    public boolean isDisyllable() {
        return false;
    }
}
