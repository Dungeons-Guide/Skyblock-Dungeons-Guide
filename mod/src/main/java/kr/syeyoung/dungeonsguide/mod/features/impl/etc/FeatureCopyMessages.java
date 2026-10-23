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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import kr.syeyoung.dungeonsguide.mod.events.impl.ChatReceivedEvent;
import net.minecraft.network.chat.Component;

public class FeatureCopyMessages extends SimpleFeature {
    public FeatureCopyMessages() {
        super("Misc.Chat Utils", "Copy Chat Messages", "Click on copy to copy", "etc.copymsg");
        setEnabled(false);
    }
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onChat(ChatReceivedEvent clientChatReceivedEvent) {
        
        if (clientChatReceivedEvent.type == 2) return;

        // TODO: 1.8.9 appended to every chat message; 26.1 has no modify hook for player chat (FabricEventBridge), so signed player chat gets no [Copy]
        clientChatReceivedEvent.message = clientChatReceivedEvent.message.copy().append(Component.literal("   §7[Copy]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand(TextUtils.stripColor(TextUtils.getFormattedText(clientChatReceivedEvent.message)))).withHoverEvent(new HoverEvent.ShowText(Component.literal("§eCopy Message")))));
    }
}
