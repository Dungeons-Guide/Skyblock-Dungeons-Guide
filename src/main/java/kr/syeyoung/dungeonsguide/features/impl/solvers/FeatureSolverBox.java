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

public class FeatureSolverBox extends SimpleFeature {
    public FeatureSolverBox() {
        super("Dungeon.Solvers.Floor 3+", "Box (Advanced)", "Calculates solution for box puzzle room, and displays it to user",  "solver.box");
        this.parameters = new LinkedHashMap<>();
        addParameter("disableText", new FeatureParameter<Boolean>("disableText", "Box Puzzle Solver Disable text", "Disable 'Type recalc to recalculate solution' showing up on top left.\nYou can still type recalc to recalc solution after disabling this feature", false, "boolean"));
        addParameter("lineColor", new FeatureParameter<AColor>("lineColor", "Line Color", "Color of the solution line", new AColor(0xFF00FF00, true), "acolor"));
        addParameter("lineWidth", new FeatureParameter<Float>("lineWidth", "Line Thickness", "Thickness of the solution line",1.0f, "float"));

        addParameter("targetColor", new FeatureParameter<AColor>("targetColor", "Target Color", "Color of the target button", new AColor(0x5500FFFF, true), "acolor"));
        addParameter("textColor1", new FeatureParameter<AColor>("textColor1", "Text Color", "Color of the text (next step)", new AColor(0xFF00FF00, true), "acolor"));
        addParameter("textColor2", new FeatureParameter<AColor>("textColor2", "Text Color", "Color of the text (others)", new AColor(0xFF000000, true), "acolor"));
    }
    public AColor getLineColor() {
        return this.<AColor>getParameter("lineColor").getValue();
    }
    public float getLineWidth() {
        return this.<Float>getParameter("lineWidth").getValue();
    }
    public boolean disableText() {
        return this.<Boolean>getParameter("disableText").getValue();
    }
    public AColor getTargetColor() {
        return this.<AColor>getParameter("targetColor").getValue();
    }
    public AColor getTextColor() {
        return this.<AColor>getParameter("textColor1").getValue();
    }
    public AColor getTextColor2() {
        return this.<AColor>getParameter("textColor2").getValue();
    }
}
