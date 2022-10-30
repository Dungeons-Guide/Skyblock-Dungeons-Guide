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

package kr.syeyoung.dungeonsguide.dungeon.data;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class DungeonRoomInfo implements Serializable {
    private static final long serialVersionUID = -8291811286448196640L;

    public DungeonRoomInfo(short shape, byte color) {
        this.uuid = UUID.randomUUID();
        this.name = this.uuid.toString();
        this.shape = shape;
        this.color = color;
    }

    private transient boolean registered;

    private boolean isUserMade = false;

    private short shape;
    private byte color;

    private int[][] blocks;

    private UUID uuid;
    private String name;

    private String processorId = "default";

    private Map<String, Object> properties = new HashMap<>();

    private Map<String, DungeonMechanic> mechanics = new HashMap<>();
    private int totalSecrets = -1;
}
