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

package kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams;

import lombok.*;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

@RequiredArgsConstructor
public class Team {
    @Getter
    private final String teamName;
    @Getter @Setter
    private String prefix;
    @Getter @Setter
    private String suffix;
    @Getter @Setter
    private String displayName;

    @Getter @Setter
    private EnumChatFormatting color;
    @Getter @Setter
    private NameTagVisibility nameTagVisibility;

    private Set<String> players = new HashSet<>();

    public Set<String> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public void addTeamMember(String player) {
        players.add(player);
        TeamManager.INSTANCE.registerTeamMember(this, player);
    }
    public void removeTeamMember(String player) {
        players.remove(player);
        TeamManager.INSTANCE.unregisterTeamMember( player);
    }
}
