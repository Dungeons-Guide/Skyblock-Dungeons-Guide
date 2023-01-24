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

package kr.syeyoung.dungeonsguide.mod.dungeon.events;

import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonCryptBrokenEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonSecretCountChangeEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class DungeonEventRecorder {
    private int latestSecretCnt = 0;
    private int latestTotalSecret = 0;
    private int latestCrypts = 0;

    @Getter
    private final List<DungeonEvent> events = new ArrayList<>();


    public void tick() {
        if (latestSecretCnt != FeatureRegistry.DUNGEON_SECRETS.getSecretsFound()) {
            int newSecretCnt = FeatureRegistry.DUNGEON_SECRETS.getSecretsFound();
            createEvent(new DungeonSecretCountChangeEvent(latestSecretCnt, newSecretCnt, latestTotalSecret, FeatureRegistry.DUNGEON_SECRETS.sureOfTotalSecrets()));
            latestSecretCnt = newSecretCnt;
        }
        if (latestTotalSecret != FeatureRegistry.DUNGEON_SECRETS.getTotalSecretsInt()) {
            latestTotalSecret = FeatureRegistry.DUNGEON_SECRETS.getTotalSecretsInt();
            createEvent(new DungeonSecretCountChangeEvent(latestSecretCnt, latestSecretCnt, latestTotalSecret, FeatureRegistry.DUNGEON_SECRETS.sureOfTotalSecrets()));
        }
        if (latestCrypts != FeatureRegistry.DUNGEON_TOMBS.getTombsFound()) {
            int newLatestCrypts = FeatureRegistry.DUNGEON_TOMBS.getTombsFound();
            createEvent(new DungeonCryptBrokenEvent(latestCrypts, newLatestCrypts));
            this.latestCrypts = newLatestCrypts;
        }
    }

    public void createEvent(DungeonEventData eventData) {
        events.add(new DungeonEvent(eventData));
    }
}
