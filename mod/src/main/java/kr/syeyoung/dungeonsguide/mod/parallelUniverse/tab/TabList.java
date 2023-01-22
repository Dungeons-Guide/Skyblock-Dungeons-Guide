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

package kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab;

import com.google.common.collect.BiMap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Ordering;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Score;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.Team;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.TeamManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.world.WorldSettings;

import java.util.*;

public class TabList {
    public static final TabList INSTANCE = new TabList();

    private final SortedSet<TabListEntry> tabListEntries = new TreeSet<>(Ordering.from((compare1, compare2) -> {
        Team scoreplayerteam = TeamManager.INSTANCE.getPlayerTeam(compare1.getGameProfile().getName());
        Team scoreplayerteam1 = TeamManager.INSTANCE.getPlayerTeam(compare2.getGameProfile().getName());
        return ComparisonChain.start()
                .compareTrueFirst(compare1.getGamemode() != WorldSettings.GameType.SPECTATOR,
                        compare2.getGamemode() != WorldSettings.GameType.SPECTATOR)
                .compare(scoreplayerteam != null ? scoreplayerteam.getTeamName() : "",
                        scoreplayerteam1 != null ? scoreplayerteam1.getTeamName() : "")
                .compare(compare1.getGameProfile().getName(), compare2.getGameProfile().getName()).result();
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

    public SortedSet<TabListEntry> getTabListEntries() {
        return Collections.unmodifiableSortedSet(tabListEntries);
    }

    public void updateEntry(TabListEntry tabListEntry) {
        removeEntry(tabListEntry.getGameProfile().getId());
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
        if (!registered.containsKey(tabListEntry.getGameProfile().getId())) {
            registered.put(tabListEntry.getGameProfile().getId(), tabListEntry);
            playerMap.put(tabListEntry.getGameProfile().getName(), tabListEntry.getGameProfile().getId());
            tabListEntries.add(tabListEntry);
        }
    }
}
