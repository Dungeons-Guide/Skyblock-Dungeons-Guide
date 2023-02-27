/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret;

import kr.syeyoung.dungeonsguide.mod.config.types.TCEnum;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class FeaturePathfindStrategy extends SimpleFeature {
    public FeaturePathfindStrategy() {
        super("Pathfinding & Secrets", "Pathfind Algorithm", "Select pathfind algorithm used by paths", "secret.secretpathfind.algorithm", true);
        addParameter("strategy", new FeatureParameter<PathfindStrategy>("strategy", "Pathfind Strategy", "Pathfind Strategy", PathfindStrategy.THETA_STAR, new TCEnum<>(PathfindStrategy.values()), neu -> {
            if (this.parameters.containsKey("strategy")) this.<PathfindStrategy>getParameter("strategy").setDescription(neu.getDescription());
        }));
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    @Getter @RequiredArgsConstructor
    public enum PathfindStrategy {
        THETA_STAR("The default pathfinding algorithm. It will generate sub-optimal path quickly."),
        A_STAR_DIAGONAL("New pathfinding algorithm. It will generate path that looks like the one JPS generates"),
        A_STAR_FINE_GRID("New pathfinding algorithm. It will generate path that kind of looks like stair");
        private final String description;
    }

    public PathfindStrategy getPathfindStrat() {
        return FeaturePathfindStrategy.this.<PathfindStrategy>getParameter("strategy").getValue();
    }
}
