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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class MapConfiguration {
    private double mapScale;
    private MapRotation mapRotation = MapRotation.VERTICAL;
    private PlayerHeadSettings selfSettings = new PlayerHeadSettings();
    private PlayerHeadSettings teammateSettings = new PlayerHeadSettings();

    private boolean drawName;

    private AColor backgroundColor;
    private AColor border;
    private double borderWidth;

    private RoomInfoSettings checkmarkSettings = new RoomInfoSettings();

    private NameSettings nameSettings = new NameSettings();

    private Map<UUID, RoomOverride> roomOverrides = new HashMap<>();

    @Data
    public static class RoomOverride {
        private boolean drawName = false;
        private String nameOverride = "";
        private String iconLocation = "";
        private String textureLocation = "";
        private RoomInfoSettings.IconRotation iconRotation = RoomInfoSettings.IconRotation.SNAP;
        private RoomInfoSettings.Style style = RoomInfoSettings.Style.ICON;
    }

    @Data
    public static class NameSettings {
        private boolean drawName;
        private AColor textColor;
        private double size;
        private double padding;
        private NameRotation nameRotation = NameRotation.SNAP;
        public enum NameRotation {
            ROTATE, FIX, SNAP, SNAP_LONG
        }
    }
    @Data
    public static class RoomInfoSettings {
        private double scale;
        private IconRotation iconRotation = IconRotation.FIX;

        private Style style;
        private boolean center;
        public enum IconRotation {
            ROTATE, FIX, SNAP
        }

        @AllArgsConstructor @Getter
        public enum Style {
            ICON("Checkmark or Resource Pack icon"),
            SECRET_COUNT("Colored Secret Count Only"),
            CHECKMARK_AND_COUNT("Draw Icon And Secret Count");

            final String description;
        }
    }

    @Data
    public static class PlayerHeadSettings {
        private IconType iconType = IconType.HEAD;
        private double iconSize;

        public enum IconType {
            NONE, HEAD, HEAD_FLIP, ARROW
        }
    }

    public enum MapRotation {
        VERTICAL, ROTATE, CENTER_ROTATE, CENTER
    }

}
