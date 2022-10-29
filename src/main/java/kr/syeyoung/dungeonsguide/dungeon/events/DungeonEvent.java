/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.events;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import lombok.Data;

import java.io.Serializable;

@Data
public class DungeonEvent implements Serializable {
    private long UTCTime = System.currentTimeMillis();
    private long realTimeElapsed;
    private long skyblockTimeElapsed;

    private String eventName;
    private DungeonEventData data;

    public DungeonEvent(DungeonEventData eventData){
        this.data = eventData;
        this.realTimeElapsed = FeatureRegistry.getInstance().DUNGEON_REALTIME.getTimeElapsed();
        this.skyblockTimeElapsed = FeatureRegistry.getInstance().DUNGEON_SBTIME.getTimeElapsed();
        this.eventName = eventData.getEventName();
    }
}
