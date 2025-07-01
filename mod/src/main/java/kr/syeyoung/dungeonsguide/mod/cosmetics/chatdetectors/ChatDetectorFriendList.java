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
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class ChatDetectorFriendList implements IChatDetector {
    @Override
    public List<ReplacementContext> getReplacementContext(Component chatComponent) {
        boolean friend = false;
        int idx = 0;

        for (Component iChatComponent : chatComponent.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            if (!(iChatComponent instanceof TextComponent)) continue;
            idx++;
            if (((TextComponent)iChatComponent).content().startsWith(" §6Friends ")) {
                friend = true;
            }
            if (idx > 5 && !friend) return null;
        }

        if (!friend) return null;

        String formatted = TextUtils.getNearestFormattedText(chatComponent);
        String strip = TextUtils.stripColor(formatted);
        int len = 0;
        List<ReplacementContext> replacementContexts = new ArrayList<>();
        for (String s : strip.split("\n")) {
            if (s.length() == 0 || s.charAt(0) == '-' || s.charAt(0) == ' ') {
                len += s.length()+1;
                continue;
            }
            String username = s.split(" ")[0];
            System.out.println(username);
            replacementContexts.add(new ReplacementContext(
                    len, username, null
            ));
            len += s.length()+1;
        }

        return replacementContexts;
    }
}
