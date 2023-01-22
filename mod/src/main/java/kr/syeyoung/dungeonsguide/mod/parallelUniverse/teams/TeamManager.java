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

import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    public static final TeamManager INSTANCE = new TeamManager();

    private Map<String, Team> registeredTeams = new HashMap<>();

    private Map<String, Team> player2team = new HashMap<>();

    public void createTeam(Team team) {
        registeredTeams.put(team.getTeamName(), team);
        for (String player : team.getPlayers()) {
            player2team.put(player, team);
        }
    }

    public void removeTeam(String teamName) {
        Team team = registeredTeams.remove(teamName);
        if (team != null) {
            for (String player : team.getPlayers()) {
                player2team.remove(player);
            }
        }
    }

    public Team getTeamByName(String teamName) {
        return registeredTeams.get(teamName);
    }

    public Team getPlayerTeam(String playerName){
        return player2team.get(playerName);
    }

    public void registerTeamMember(Team team, String player) {
        if (!registeredTeams.containsKey(team.getTeamName())) return;
        UUID uuid = TabList.INSTANCE.getPlayer(player);
        TabListEntry tabListEntry = TabList.INSTANCE.getEntry(uuid);
        if (tabListEntry != null) {
            TabList.INSTANCE.removeEntry(uuid);
        }
        player2team.put(player, team);
        if (tabListEntry != null) {
            TabList.INSTANCE.updateEntry(tabListEntry);
        }
    }
    public void unregisterTeamMember(String player) {
        if (!player2team.containsKey(player)) return;
        UUID uuid = TabList.INSTANCE.getPlayer(player);
        TabListEntry tabListEntry = TabList.INSTANCE.getEntry(uuid);
        if (tabListEntry != null) {
            TabList.INSTANCE.removeEntry(uuid);
        }
        player2team.remove(player);
        if (tabListEntry != null) {
            TabList.INSTANCE.updateEntry(tabListEntry);
        }
    }


    public void clear() {
        registeredTeams.clear();
        player2team.clear();
    }
}
