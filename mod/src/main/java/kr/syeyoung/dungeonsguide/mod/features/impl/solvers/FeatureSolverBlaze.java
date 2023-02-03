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

package kr.syeyoung.dungeonsguide.mod.features.impl.solvers;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCAColor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;

public class FeatureSolverBlaze extends SimpleFeature {
    public FeatureSolverBlaze() {
        super("Dungeon.Solvers.Floor 2+", "Blaze", "Highlights the blaze that needs to be killed in an blaze room", "solver.blaze");
        addParameter("normBlazeColor", new FeatureParameter<AColor>("blazecolor", "Normal Blaze Color", "Normal Blaze Color", new AColor(255,255,255,255), TCAColor.INSTANCE, nval -> normBlazeColor = nval));
        addParameter("nextBlazeColor", new FeatureParameter<AColor>("blazecolor", "Next Blaze Color", "Next Blaze Color", new AColor(0,255,0,255), TCAColor.INSTANCE, nval -> nextBlazeColor = nval));
        addParameter("nextUpBlazeColor", new FeatureParameter<AColor>("blazecolor", "Next Up Blaze Color", "Color of blaze after next blaze", new AColor(255,255,0,255), TCAColor.INSTANCE, nval -> nextUpBlazeColor = nval));
        addParameter("blazeborder", new FeatureParameter<AColor>("blazeborder", "Blaze Border Color", "Blaze border color", new AColor(255,255,255,0), TCAColor.INSTANCE, nval -> blazeBorder = nval));
    }

    AColor normBlazeColor;
    AColor nextBlazeColor;
    AColor nextUpBlazeColor;
    AColor blazeBorder;

    public AColor getBlazeColor() {
        return normBlazeColor;
    }
    public AColor getNextBlazeColor() {
        return nextBlazeColor;
    }
    public AColor getNextUpBlazeColor() {
        return nextUpBlazeColor;
    }
    public AColor getBorder() {
        return blazeBorder;
    }
}
