/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapConfiguration {
    private double mapScale;
    private MapRotation mapRotation;
    private PlayerHeadSettings selfSettings;
    private PlayerHeadSettings otherSettings;

    private boolean centerCheckmarks;
    private boolean drawName;
    private boolean drawSecrets;

    private AColor backgroundColor;
    private AColor border;
    private double borderWidth;

    public static class PlayerHeadSettings {
        private IconType iconType;
        private double iconSize;

        public enum IconType {
            NONE, PLAYER_HEAD, ARROW
        }
    }

    public enum MapRotation {
        NORMAL, CENTER, CENTER_ROTATE
    }

}
