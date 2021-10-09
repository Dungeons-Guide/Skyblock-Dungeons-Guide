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

public class FeatureSolverBlaze extends SimpleFeature {
    public FeatureSolverBlaze() {
        super("Solver.Floor 2+", "Blaze", "Highlights the blaze that needs to be killed in an blaze room", "solver.blaze");
        parameters.put("normBlazeColor", new FeatureParameter<AColor>("blazecolor", "Normal Blaze Color", "Normal Blaze Color", new AColor(255,255,255,255), "acolor"));
        parameters.put("nextBlazeColor", new FeatureParameter<AColor>("blazecolor", "Next Blaze Color", "Next Blaze Color", new AColor(0,255,0,255), "acolor"));
        parameters.put("nextUpBlazeColor", new FeatureParameter<AColor>("blazecolor", "Next Up Blaze Color", "Color of blaze after next blaze", new AColor(255,255,0,255), "acolor"));
        parameters.put("blazeborder", new FeatureParameter<AColor>("blazeborder", "Blaze Border Color", "Blaze border color", new AColor(255,255,255,0), "acolor"));
    }

    public AColor getBlazeColor() {
        return this.<AColor>getParameter("normBlazeColor").getValue();
    }
    public AColor getNextBlazeColor() {
        return this.<AColor>getParameter("nextBlazeColor").getValue();
    }
    public AColor getNextUpBlazeColor() {
        return this.<AColor>getParameter("nextUpBlazeColor").getValue();
    }
    public AColor getBorder() {
        return this.<AColor>getParameter("blazeborder").getValue();
    }
}
