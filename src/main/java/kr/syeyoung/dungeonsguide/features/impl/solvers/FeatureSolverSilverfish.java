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

import java.util.LinkedHashMap;

public class FeatureSolverSilverfish extends SimpleFeature {
    public FeatureSolverSilverfish() {
        super("Solver.Floor 3+", "Silverfish (Advanced)", "Actively calculates solution for silverfish puzzle and displays it to user",  "solver.silverfish");
        this.parameters = new LinkedHashMap<>();
        this.parameters.put("lineColor", new FeatureParameter<AColor>("lineColor", "Line Color", "Color of the solution line", new AColor(0xFF00FF00, true), "acolor"));
        this.parameters.put("lineWidth", new FeatureParameter<Float>("lineWidth", "Line Thickness", "Thickness of the solution line",1.0f, "float"));
    }
    public AColor getLineColor() {
        return this.<AColor>getParameter("lineColor").getValue();
    }
    public float getLineWidth() {
        return this.<Float>getParameter("lineWidth").getValue();
    }
}
