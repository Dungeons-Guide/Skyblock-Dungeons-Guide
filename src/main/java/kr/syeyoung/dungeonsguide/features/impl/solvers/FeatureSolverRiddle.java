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

public class FeatureSolverRiddle extends SimpleFeature {
    public FeatureSolverRiddle() {
        super("Solver.Any Floor", "Riddle", "Highlights the correct box after clicking on all 3 weirdos",  "solver.riddle");

        this.parameters.put("targetColor", new FeatureParameter<AColor>("targetColor", "Target Color", "Color of the solution box", new AColor(0,255,0,50), "acolor"));
    }

    public AColor getTargetColor() {
        return this.<AColor>getParameter("targetColor").getValue();
    }
}
