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
import kr.syeyoung.dungeonsguide.mod.events.impl.DGChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import net.kyori.adventure.text.Component;

public class FeatureCopyMessages extends SimpleFeature {
    public FeatureCopyMessages() {
        super("Misc.Chat Utils", "Copy Chat Messages", "Click on copy to copy", "etc.copymsg");
        setEnabled(false);
    }

    private boolean shouldSuggest = !ModAPI.getAPI().getPlatform().supportCopyClickEvent();
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onChat(DGChatReceivedEvent clientChatReceivedEvent) {
        clientChatReceivedEvent.setChat(
                clientChatReceivedEvent.getChat()
                        .append(Component
                                .text("   §7[Copy]")
                                .clickEvent(
                                        shouldSuggest ?
                                        net.kyori.adventure.text.event.ClickEvent.suggestCommand(TextUtils.stripColor(clientChatReceivedEvent.getOriginalFormattedText())):
                                        net.kyori.adventure.text.event.ClickEvent.copyToClipboard(TextUtils.stripColor(clientChatReceivedEvent.getOriginalFormattedText()))))
        );
    }
}
