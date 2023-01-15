/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;



import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class FeatureCopyMessages extends SimpleFeature {
    public FeatureCopyMessages() {
        super("Misc.Chat", "Copy Chat Messages", "Click on copy to copy", "etc.copymsg");
        setEnabled(false);
    }
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        
        if (clientChatReceivedEvent.type == 2) return;

        clientChatReceivedEvent.message.appendSibling(new ChatComponentText("   §7[Copy]").setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtils.stripColor(clientChatReceivedEvent.message.getFormattedText()))).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§eCopy Message")))));
    }
}
