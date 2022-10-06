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
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;

import java.util.LinkedHashMap;

public class PathfindLineProperties extends SimpleFeature {
    private PathfindLineProperties parent;
    public PathfindLineProperties(String category, String name, String description, String key, boolean useParent, PathfindLineProperties parent) {
        super(category, name, description, key);
        this.parent = parent;
        this.parameters = new LinkedHashMap<>();
        if (parent != null)
            addParameter("useGlobal", new FeatureParameter<Boolean>("useGlobal", "Use Global Settings instead of this", "Completely ignore these settings, then use the parent one:: '"+parent.getName()+"'",  useParent, "boolean"));
        addParameter("pathfind", new FeatureParameter<Boolean>("pathfind", "Enable Pathfinding", "Enable pathfind for secrets",  useParent, "boolean"));
        addParameter("lineColor", new FeatureParameter<AColor>("lineColor", "Line Color", "Color of the pathfind line", new AColor(0xFFFF0000, true), "acolor"));
        addParameter("lineWidth", new FeatureParameter<Float>("lineWidth", "Line Thickness", "Thickness of the pathfind line",1.0f, "float"));
        addParameter("linerefreshrate", new FeatureParameter<Integer>("linerefreshrate", "Line Refreshrate", "Ticks to wait per line refresh. Specify it to -1 to don't refresh line at all", 10, "integer"));
        addParameter("beacon", new FeatureParameter<Boolean>("beacon", "Enable Beacons", "Enable beacons for pathfind line targets",  true, "boolean"));
        addParameter("beamColor", new FeatureParameter<AColor>("beamColor", "Beam Color", "Color of the beacon beam", new AColor(0x77FF0000, true), "acolor"));
        addParameter("beamTargetColor", new FeatureParameter<AColor>("beamTargetColor", "Target Color", "Color of the target", new AColor(0x33FF0000, true), "acolor"));
    }


    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                MFeatureEdit featureEdit = new MFeatureEdit(PathfindLineProperties.this, rootConfigPanel);
                for (FeatureParameter parameter: getParameters()) {
                    if (parameter.getKey().startsWith("line"))
                        featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(PathfindLineProperties.this, parameter, rootConfigPanel, a -> isGlobal() || !isPathfind()));
                    else if (parameter.getKey().startsWith("beam"))
                        featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(PathfindLineProperties.this, parameter, rootConfigPanel, a -> isGlobal() || !isBeacon()));
                    else if (!parameter.getKey().equals("useGlobal"))
                        featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(PathfindLineProperties.this, parameter, rootConfigPanel, a -> isGlobal()));
                    else
                        featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(PathfindLineProperties.this, parameter, rootConfigPanel, a -> false));
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
        if (parent == null) return false;
        return this.<Boolean>getParameter("useGlobal").getValue();
    }

    public boolean isPathfind() {
        return isGlobal() ? parent.isPathfind() : this.<Boolean>getParameter("pathfind").getValue();
    }
    public AColor getLineColor() {
        return isGlobal() ? parent.getLineColor() : this.<AColor>getParameter("lineColor").getValue();
    }
    public float getLineWidth() {
        return isGlobal() ? parent.getLineWidth() : this.<Float>getParameter("lineWidth").getValue();
    }
    public int getRefreshRate() {
        return isGlobal() ? parent.getRefreshRate() : this.<Integer>getParameter("linerefreshrate").getValue();
    }
    public boolean isBeacon() {
        return isGlobal() ? parent.isBeacon() : this.<Boolean>getParameter("beacon").getValue();
    }
    public AColor getBeamColor() {
        return isGlobal() ? parent.getBeamColor() : this.<AColor>getParameter("beamColor").getValue();
    }
    public AColor getTargetColor() {
        return isGlobal() ? parent.getTargetColor() : this.<AColor>getParameter("beamTargetColor").getValue();
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
