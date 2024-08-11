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

import kr.syeyoung.dungeonsguide.mod.config.types.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;

public class FeatureSolverWaterboard extends SimpleFeature {
    public FeatureSolverWaterboard() {
        super("Puzzle Solvers", "Waterboard (Oneflow)", "Calculates solution for waterboard puzzle and displays it to user. Dungeons Guide is able to calculate one-flow solution within reasonable amount of time. Tune hyperparameters within this solver. \n\nYou need to click lever at or after timer hits 0. Do not flip lever before it hits 0. The solver has been coded so that the timer is the absolute minimum time you need to wait before hitting that lever.", "solver.waterboard");
        addParameter("block", new FeatureParameter<Boolean>("block", "Block wrong clicks", "Block wrong clicks", true, TCBoolean.INSTANCE));


        addParameter("tempMult", new FeatureParameter<Double>("tempMult", "Temperature Multiplier", "Temperature Multiplier for simulated annealing. Values >= 1 will break solver, Values < 0 will make solver 2x slower, and Values closer to 1 will generate faster solutions slower, where as values closer to 0 will generate slower solutions faster", 0.9999, TCDouble.INSTANCE));
        addParameter("targetTemp", new FeatureParameter<Double>("targetTemp", "Temperature Target", "Temperature Target. Negative values or values > 2.3 will make solver freeze. Higher value means slower solutions faster, and lower value means faster solution slower.", 0.01, TCDouble.INSTANCE));
        addParameter("iterTarget", new FeatureParameter<Integer>("iterTarget", "Iteration Target", "If for given iteration the solution does not get better, the solver will return current best solution. Lower value means slower solutions faster, and higher value means faster solutions slower", 3000, TCInteger.INSTANCE));

        addParameter("moves", new FeatureParameter<Integer>("moves", "Lever flip waits", "Minimum time to wait between lever flips. This value is in water ticks, which is the time it takes for water to flow one block. This value / 4 equals seconds. Default is 1s. Dev thinks 3 might be better, but dev's aim wasn't good enough to keep up with min wait=3 solution", 4, TCInteger.INSTANCE));
        addParameter("cnt1", new FeatureParameter<Integer>("cnt1", "Maximum amount of 1-water-tick passes", "Number of no-op actions. Higher value means it is able to find slower but only solution slower, where as lower value means it can find faster solution faster but it will fail if it does not exist.", 45, TCInteger.INSTANCE));
        addParameter("cnt2", new FeatureParameter<Integer>("cnt2", "Maximum amount of lever flips per each lever type", "Higher value means faster complex solution slower, and lower value means ... no solution.", 3, TCInteger.INSTANCE));


    }




    public boolean blockClicks() {
        return this.<Boolean>getParameter("block").getValue();
    }

    public double getTempMult() {
        return this.<Double>getParameter("tempMult").getValue();
    }
    public double getTargetTemp() {
        return this.<Double>getParameter("targetTemp").getValue();
    }
    public int getIterTarget() {
        return this.<Integer>getParameter("iterTarget").getValue();
    }
    public int getMoves() {
        return this.<Integer>getParameter("moves").getValue();
    }
    public int getCnt1() {
        return this.<Integer>getParameter("cnt1").getValue();
    }
    public int getCnt2() {
        return this.<Integer>getParameter("cnt2").getValue();
    }
}
