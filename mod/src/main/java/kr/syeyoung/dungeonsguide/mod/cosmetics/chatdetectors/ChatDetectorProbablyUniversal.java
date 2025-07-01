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
import kr.syeyoung.modapi.ModAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.Collections;
import java.util.List;

public class ChatDetectorProbablyUniversal implements IChatDetector {
    @Override
    public List<ReplacementContext> getReplacementContext(Component chatComponent) {
        String formatted = TextUtils.getNearestFormattedText(chatComponent);
        if (!formatted.contains(": ")) return null;

        boolean correspondingEvFound = false;
        for (Component component : chatComponent.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            ClickEvent ev = component.clickEvent();
            if (ev != null && ev.action() == ClickEvent.Action.RUN_COMMAND) {
                if (ev.value().startsWith("/msg")) correspondingEvFound = true;
                if (ev.value().startsWith("/socialoptions")) correspondingEvFound = true;
                if (ev.value().startsWith("/viewprofile")) correspondingEvFound = true;
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
            if (potentialName.equalsIgnoreCase(ModAPI.getAPI().getSession().getUsername()))
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
