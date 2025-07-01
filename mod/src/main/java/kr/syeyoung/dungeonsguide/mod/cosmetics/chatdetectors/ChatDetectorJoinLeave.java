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
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.List;

public class ChatDetectorJoinLeave implements IChatDetector {
    @Override
    public List<ReplacementContext> getReplacementContext(Component chatComponent) {
        String formatted = TextUtils.getNearestFormattedText(chatComponent);
        if (formatted.startsWith("§aFriend > ")) {
            if (formatted.endsWith("§eleft.") || formatted.endsWith("§ejoined.")) {
                String strip = TextUtils.stripColor(formatted);
                String username = strip.substring(9, strip.indexOf(' ', 9));
                return Collections.singletonList(new ReplacementContext(
                        9, username, null
                ));
            }
        } else if (formatted.startsWith("§2Guild > §6")) {
            if (formatted.endsWith("§eleft.") || formatted.endsWith("§ejoined.")) {
                String strip = TextUtils.stripColor(formatted);
                String username = strip.substring(8, strip.indexOf(' ', 8));
                return Collections.singletonList(new ReplacementContext(
                        8, username, null
                ));
            }
        }  else if (formatted.endsWith("§6joined the lobby!")) {
            String[] messageSplit = TextUtils.stripColor(formatted).split(" ");
            String oldLeader = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                oldLeader = s;
                break;
            }
            if (oldLeader != null)
                return Collections.singletonList(new ReplacementContext(
                        TextUtils.stripColor(formatted).indexOf(oldLeader), oldLeader, null
                ));
        } else if (formatted.endsWith("§6joined the lobby! §a<§c<§b<")) {
            String[] messageSplit = TextUtils.stripColor(formatted.substring(15)).split(" ");
            String oldLeader = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                oldLeader = s;
                break;
            }
            if (oldLeader != null)
                return Collections.singletonList(new ReplacementContext(
                        TextUtils.stripColor(formatted).indexOf(oldLeader), oldLeader, null
                ));
        } else if (formatted.startsWith("§b✦ ") && formatted.contains("§7found a ") && formatted.endsWith("§bMystery Box§7!")) {
            String[] messageSplit = TextUtils.stripColor(formatted.substring(4)).split(" ");
            String oldLeader = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                oldLeader = s;
                break;
            }
            if (oldLeader != null)
                return Collections.singletonList(new ReplacementContext(
                        TextUtils.stripColor(formatted).indexOf(oldLeader), oldLeader, null
                ));
        }
        return null;
    }
}
