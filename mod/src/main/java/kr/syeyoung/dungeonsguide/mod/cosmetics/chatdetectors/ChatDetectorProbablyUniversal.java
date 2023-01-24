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

package kr.syeyoung.dungeonsguide.mod.cosmetics.chatdetectors;

import kr.syeyoung.dungeonsguide.mod.cosmetics.surgical.ReplacementContext;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatDetectorProbablyUniversal implements IChatDetector {
    @Override
    public List<ReplacementContext> getReplacementContext(IChatComponent chatComponent) {
        String formatted = chatComponent.getFormattedText();
        if (!formatted.contains(": ")) return null;


        boolean correspondingEvFound = false;
        for (IChatComponent iChatComponent : chatComponent) {
            ClickEvent ev = iChatComponent.getChatStyle().getChatClickEvent();
            if (ev != null) {
                if (ev.getValue().startsWith("/msg")) correspondingEvFound = true;
                if (ev.getValue().startsWith("/socialoptions")) correspondingEvFound = true;
                if (ev.getValue().startsWith("/viewprofile")) correspondingEvFound = true;
            }
        }

        formatted = formatted.substring(0, formatted.indexOf(": "));
        String name = TextUtils.stripColor(formatted);

        String[] splited = name.split(" ");
        int backLen = 0;
        label: for (int i = splited.length - 1; i >= 0; i--) {
            String potentialName = splited[i];
            backLen += potentialName.length() + 1;
            for (char c : potentialName.toCharArray()) {
                if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || (c == '_' || c == '-')) {
                    continue;
                }
                continue label;
            }
            if (potentialName.equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.getName()))
                return Collections.singletonList(new ReplacementContext(
                        name.length() - backLen, potentialName, null
                ));
            else if (correspondingEvFound)
                return Collections.singletonList(new ReplacementContext(
                        name.length() - backLen, potentialName, null
                ));
            else return null;
        }
        return null;
    }
}
