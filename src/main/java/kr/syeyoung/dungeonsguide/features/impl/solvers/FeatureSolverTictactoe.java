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

package kr.syeyoung.dungeonsguide.features.impl.solvers;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;

public class FeatureSolverTictactoe extends SimpleFeature {
    public FeatureSolverTictactoe() {
        super("Dungeon.Solvers.Any Floor", "Tictactoe", "Shows the best move that could be taken by player in the tictactoe room", "solver.tictactoe");

        addParameter("targetColor", new FeatureParameter<AColor>("targetColor", "Target Color", "Color of the solution box during your turn", new AColor(0,255,255,50), "acolor"));
        addParameter("targetColor2", new FeatureParameter<AColor>("targetColor", "Target Color", "Color of the solution box during enemy turn", new AColor(255,201,0,50), "acolor"));
    }

    public AColor getTargetColor() {
        return this.<AColor>getParameter("targetColor").getValue();
    }
    public AColor getTargetColor2() {
        return this.<AColor>getParameter("targetColor2").getValue();
    }
}
