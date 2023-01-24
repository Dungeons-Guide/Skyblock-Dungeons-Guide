/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatDetectorGuildPartyList implements IChatDetector {
    @Override
    public List<ReplacementContext> getReplacementContext(IChatComponent chatComponent) {
        String formatted = chatComponent.getFormattedText();
        if (!(formatted.contains("§c ● ") || formatted.contains("§a ● ") ||
                formatted.contains("§c●") || formatted.contains("§a●"))) {
            return null;
        }
//
        String strip = TextUtils.stripColor(formatted);
//        String last = strip.split(": ");

        List<String> players = new ArrayList<>();
        for (String s : strip.split(" ")) {
            if (s.contains("●")) continue;
            if (s.startsWith("[")) continue;
            if (s.endsWith(":")) continue;

            if (!(formatted.contains(s+"§r") || formatted.contains(s+" §r") )) continue;
            players.add(s);
        }

        return players.stream().map(a -> new ReplacementContext(
                strip.indexOf(a), a, null
        )).collect(Collectors.toList());
    }
}
