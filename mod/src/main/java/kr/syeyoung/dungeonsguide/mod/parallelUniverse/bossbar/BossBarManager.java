/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.parallelUniverse.bossbar;

import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

// 1.8.9 had a single BossStatus, 26.1 servers send any number of boss bars
public class BossBarManager {
    public static final BossBarManager INSTANCE = new BossBarManager();

    // insertion order is the order vanilla draws them in
    private Map<UUID, BossBar> bossBars = new LinkedHashMap<>();

    public void addBossBar(BossBar bossBar) {
        bossBars.put(bossBar.getId(), bossBar);
    }

    public void removeBossBar(UUID id) {
        bossBars.remove(id);
    }

    public BossBar getBossBar(UUID id) {
        return bossBars.get(id);
    }

    public BossBar getFirst() {
        return bossBars.isEmpty() ? null : bossBars.values().iterator().next();
    }

    public BossBar getBossBarByName(String name) {
        for (BossBar bossBar : bossBars.values()) {
            if (bossBar.getName() != null && TextUtils.stripColor(TextUtils.getFormattedText(bossBar.getName())).contains(name)) return bossBar;
        }
        return null;
    }

    public void clear() {
        bossBars.clear();
    }
}
