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
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.MFeatureEdit;
import kr.syeyoung.dungeonsguide.config.guiconfig.MParameterEdit;
import kr.syeyoung.dungeonsguide.config.guiconfig.RootConfigPanel;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MStringSelectionButton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FeaturePathfindStrategy extends SimpleFeature {
    public FeaturePathfindStrategy() {
        super("Dungeon.Secrets.Preferences", "Pathfind Algorithm", "Select pathfind algorithm used by paths", "secret.secretpathfind.algorithm", true);
        addParameter("strategy", new FeatureParameter<String>("strategy", "Pathfind Strategy", "Pathfind Strategy", "THETA_STAR", "string"));

    }

    @Override
    public boolean isDisyllable() {
        return false;
    }

    @Override
    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {

                MFeatureEdit featureEdit = new MFeatureEdit(FeaturePathfindStrategy.this, rootConfigPanel);
                PathfindStrategy alignment = getPathfindStrat();
                MStringSelectionButton mStringSelectionButton = new MStringSelectionButton(Arrays.stream(PathfindStrategy.values()).map(Enum::name).collect(Collectors.toList()), alignment.name()) {
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(150, 20);
                    }
                };
                mStringSelectionButton.setOnUpdate(() -> {
                    FeaturePathfindStrategy.this.<String>getParameter("strategy").setValue(mStringSelectionButton.getSelected());
                    FeaturePathfindStrategy.this.<String>getParameter("strategy").setDescription(getPathfindStrat().getDescription());
                    featureEdit.removeParameterEdit(null);
                });
                featureEdit.addParameterEdit("strategy", new MParameterEdit(FeaturePathfindStrategy.this, FeaturePathfindStrategy.this.<String>getParameter("strategy"), rootConfigPanel, mStringSelectionButton, (a) -> false));

                for (FeatureParameter parameter: getParameters()) {
                    if (parameter.getKey().equals("strategy")) continue;
                    featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(FeaturePathfindStrategy.this, parameter, rootConfigPanel));
                }
                return featureEdit;
            }
        });
        return "base." + getKey() ;
    }

    @Getter @RequiredArgsConstructor
    public enum PathfindStrategy {
        THETA_STAR("The default pathfinding algorithm. It will generate sub-optimal path quickly."),
        A_STAR_DIAGONAL("New pathfinding algorithm. It will generate path that looks like the one JPS generates"),
        A_STAR_FINE_GRID("New pathfinding algorithm. It will generate path that kind of looks like stair"),
        JPS_LEGACY("The improved pathfinding algorithm. Not suggested for usage. It will have problems on diagonal movements, thus giving wrong routes"),
        A_STAR_LEGACY("The first pathfinding algorithm. It may have problem on navigating through stairs. This is the one used by Minecraft for mob pathfind.");

        private final String description;
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        FeaturePathfindStrategy.PathfindStrategy alignment;
        try {
            alignment = PathfindStrategy.valueOf(FeaturePathfindStrategy.this.<String>getParameter("strategy").getValue());
        } catch (Exception e) {alignment = PathfindStrategy.THETA_STAR;}
        FeaturePathfindStrategy.this.<String>getParameter("strategy").setValue(alignment.name());
        FeaturePathfindStrategy.this.<String>getParameter("strategy").setDescription(alignment.getDescription());
    }

    public PathfindStrategy getPathfindStrat() {
        return PathfindStrategy.valueOf(FeaturePathfindStrategy.this.<String>getParameter("strategy").getValue());
    }
}
