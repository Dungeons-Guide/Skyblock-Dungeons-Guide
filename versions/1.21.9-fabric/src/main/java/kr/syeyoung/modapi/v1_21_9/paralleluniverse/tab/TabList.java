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

package kr.syeyoung.modapi.v1_21_9.paralleluniverse.tab;

import com.google.common.collect.BiMap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Ordering;
import kr.syeyoung.modapi.paralleluniverse.tablist.UTabList;
import kr.syeyoung.modapi.util.GameMode;
import kr.syeyoung.modapi.v1_21_9.paralleluniverse.teams.Team;
import kr.syeyoung.modapi.v1_21_9.paralleluniverse.teams.TeamManager;

import java.util.*;

public class TabList implements UTabList {
    public static final TabList INSTANCE = new TabList();

    private final SortedSet<TabListEntry> tabListEntries = new TreeSet<>(Ordering.from((compare1, compare2) -> {
        Team scorePlayerTeam = TeamManager.INSTANCE.getPlayerTeam(compare1.getGameProfile().name());
        Team scorePlayerTeam1 = TeamManager.INSTANCE.getPlayerTeam(compare2.getGameProfile().name());
        return ComparisonChain.start()
                .compareTrueFirst(compare1.getGameMode() != GameMode.SPECTATOR,
                        compare2.getGameMode() != GameMode.SPECTATOR)
                .compare(scorePlayerTeam != null ? scorePlayerTeam.getTeamName() : "",
                        scorePlayerTeam1 != null ? scorePlayerTeam1.getTeamName() : "")
                .compare(compare1.getGameProfile().name(), compare2.getGameProfile().name()).result();
    }));
    private final Map<UUID, TabListEntry> registered = new HashMap<>();
    private final BiMap<String, UUID> playerMap = HashBiMap.create();

    public void clear() {
        registered.clear();
        playerMap.clear();
        tabListEntries.clear();
    }

    public UUID getPlayer(String name) {
        return playerMap.get(name);
    }

    public SortedSet<? extends TabListEntry> getTabListEntries() {
        return Collections.unmodifiableSortedSet(tabListEntries);
    }

    public void updateEntry(TabListEntry tabListEntry) {
        removeEntry(tabListEntry.getGameProfile().id());
        addEntry(tabListEntry);
    }

    public TabListEntry getEntry(UUID uuid) {
        return registered.get(uuid);
    }

    public void removeEntry(UUID uuid) {
        if (registered.containsKey(uuid)) {
            TabListEntry scoreObj = registered.remove(uuid);
            playerMap.inverse().remove(uuid);
            tabListEntries.remove(scoreObj);
        }
    }
    public void addEntry(TabListEntry tabListEntry) {
        if (!registered.containsKey(tabListEntry.getGameProfile().id())) {
            registered.put(tabListEntry.getGameProfile().id(), tabListEntry);
            playerMap.put(tabListEntry.getGameProfile().name(), tabListEntry.getGameProfile().id());
            tabListEntries.add(tabListEntry);
        }
    }
}
