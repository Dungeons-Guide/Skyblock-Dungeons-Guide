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
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;

import java.util.LinkedHashMap;

public class PathfindLineProperties extends SimpleFeature {
    public PathfindLineProperties(String category, String name, String description, String key, boolean useGlobal) {
        super(category, name, description, key);
        this.parameters = new LinkedHashMap<>();
        if (!key.equals("secret.lineproperties.global"))
            this.parameters.put("useGlobal", new FeatureParameter<Boolean>("useGlobal", "Use Global Settings instead of this", "Completely ignore these settings, then use the global one",  useGlobal, "boolean"));
        this.parameters.put("pathfind", new FeatureParameter<Boolean>("pathfind", "Enable Pathfinding", "Enable pathfind for secrets",  useGlobal, "boolean"));
        this.parameters.put("lineColor", new FeatureParameter<AColor>("lineColor", "Line Color", "Color of the pathfind line", new AColor(0xFFFF0000, true), "acolor"));
        this.parameters.put("lineWidth", new FeatureParameter<Float>("lineWidth", "Line Thickness", "Thickness of the pathfind line",1.0f, "float"));
        this.parameters.put("refreshrate", new FeatureParameter<Integer>("refreshrate", "Line Refreshrate", "Ticks to wait per line refresh. Specify it to -1 to don't refresh line at all", 10, "integer"));
        this.parameters.put("beacon", new FeatureParameter<Boolean>("beacon", "Enable Beacons", "Enable beacons for pathfind line targets",  true, "boolean"));
        this.parameters.put("beamColor", new FeatureParameter<AColor>("beamColor", "Beam Color", "Color of the beacon beam", new AColor(0x77FF0000, true), "acolor"));
        this.parameters.put("targetColor", new FeatureParameter<AColor>("targetColor", "Target Color", "Color of the target", new AColor(0x33FF0000, true), "acolor"));
    }


    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                MFeatureEdit featureEdit = new MFeatureEdit(PathfindLineProperties.this, rootConfigPanel);
                for (FeatureParameter parameter: getParameters()) {
                    featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(PathfindLineProperties.this, parameter, rootConfigPanel, a -> !a.getKey().equals("useGlobal") && isGlobal()));
                }
                return featureEdit;
            }
        });
        return "base." + getKey() ;
    }

    @Override
    public boolean isDisyllable() {
        return false;
    }

    public boolean isGlobal() {
        if (getKey().equals("secret.lineproperties.global")) return false;
        return this.<Boolean>getParameter("useGlobal").getValue();
    }

    public boolean isPathfind() {
        return isGlobal() ? FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.isPathfind() : this.<Boolean>getParameter("pathfind").getValue();
    }
    public AColor getLineColor() {
        return isGlobal() ? FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.getLineColor() : this.<AColor>getParameter("lineColor").getValue();
    }
    public float getLineWidth() {
        return isGlobal() ? FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.getLineWidth() : this.<Float>getParameter("lineWidth").getValue();
    }
    public int getRefreshRate() {
        return isGlobal() ? FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.getRefreshRate() : this.<Integer>getParameter("refreshrate").getValue();
    }
    public boolean isBeacon() {
        return isGlobal() ? FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.isBeacon() : this.<Boolean>getParameter("beacon").getValue();
    }
    public AColor getBeamColor() {
        return isGlobal() ? FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.getBeamColor() : this.<AColor>getParameter("beamColor").getValue();
    }
    public AColor getTargetColor() {
        return isGlobal() ? FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.getTargetColor() : this.<AColor>getParameter("targetColor").getValue();
    }
    public ActionRoute.ActionRouteProperties getRouteProperties() {
        ActionRoute.ActionRouteProperties actionRouteProperties = new ActionRoute.ActionRouteProperties();
        actionRouteProperties.setPathfind(isPathfind());
        actionRouteProperties.setLineColor(getLineColor());
        actionRouteProperties.setLineWidth(getLineWidth());
        actionRouteProperties.setLineRefreshRate(getRefreshRate());
        actionRouteProperties.setBeacon(isBeacon());
        actionRouteProperties.setBeaconBeamColor(getBeamColor());
        actionRouteProperties.setBeaconColor(getTargetColor());
        return actionRouteProperties;
    }
}
