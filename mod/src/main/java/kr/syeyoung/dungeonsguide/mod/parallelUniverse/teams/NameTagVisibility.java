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

public enum NameTagVisibility {
    HIDE_FOR_OTHER_TEAMS,
    HIDE_FOR_OWN_TEAM,
    NEVER,
    UNKNOWN;

    public static NameTagVisibility of(String val) {
        if (val.equals("hideForOtherTeams")) return NameTagVisibility.HIDE_FOR_OTHER_TEAMS;
        if (val.equals("hideForOwnTeam")) return NameTagVisibility.HIDE_FOR_OWN_TEAM;
        if (val.equals("never")) return NameTagVisibility.NEVER;
        return NameTagVisibility.UNKNOWN;
    }
}
