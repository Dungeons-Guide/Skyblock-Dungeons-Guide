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

package kr.syeyoung.dungeonsguide.features.impl.secret;

import com.google.common.base.Supplier;
import kr.syeyoung.dungeonsguide.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.MFeatureEdit;
import kr.syeyoung.dungeonsguide.config.guiconfig.MParameterEdit;
import kr.syeyoung.dungeonsguide.config.guiconfig.RootConfigPanel;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;

public class FeatureCreateRefreshLine extends SimpleFeature {
    public FeatureCreateRefreshLine() {
        super("Dungeon Secrets.Keybinds", "Refresh pathfind line or Trigger pathfind", "A keybind for creating or refresh pathfind lines for pathfind contexts that doesn't have line, or contexts that has refresh rate set to -1.\nChange key at your key settings (Settings -> Controls)", "secret.refreshPathfind", true);

        this.parameters.put("pathfind", new FeatureParameter<Boolean>("pathfind", "Enable Pathfinding", "Force Enable pathfind for future actions when used", false, "boolean"));
        this.parameters.put("refreshrate", new FeatureParameter<Integer>("refreshrate", "Line Refreshrate", "Ticks to wait per line refresh, to be overriden. If the line already has pathfind enabled, this value does nothing. Specify it to -1 to don't refresh line at all", 10, "integer"));
    }
    public boolean isPathfind() {
        return this.<Boolean>getParameter("pathfind").getValue();
    }
    public int getRefreshRate() {
        return this.<Integer>getParameter("refreshrate").getValue();
    }


    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                MFeatureEdit featureEdit = new MFeatureEdit(FeatureCreateRefreshLine.this, rootConfigPanel);
                for (FeatureParameter parameter: getParameters()) {
                    if (parameter.getKey().equals("refreshrate"))
                        featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(FeatureCreateRefreshLine.this, parameter, rootConfigPanel, a -> !isPathfind()));
                    else
                        featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(FeatureCreateRefreshLine.this, parameter, rootConfigPanel, a -> false));
                }
                return featureEdit;
            }
        });
        return "base." + getKey() ;
    }



}
