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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret;

import kr.syeyoung.dungeonsguide.mod.config.types.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;

import java.util.LinkedHashMap;

public class PathfindLineProperties extends SimpleFeature {
    private PathfindLineProperties parent;
    public PathfindLineProperties(String category, String name, String description, String key, boolean useParent, PathfindLineProperties parent) {
        super(category, name, description, key);
        this.parent = parent;
        this.parameters = new LinkedHashMap<>();
        if (parent != null)
            addParameter("useGlobal", new FeatureParameter<Boolean>("useGlobal", "Use Global Settings instead of this", "Completely ignore these settings, then use the parent one:: '"+parent.getName()+"'",  useParent, TCBoolean.INSTANCE));
        addParameter("pathfind", new FeatureParameter<Boolean>("pathfind", "Enable Pathfinding", "Enable pathfind for secrets",  useParent, TCBoolean.INSTANCE));
        addParameter("lineColor", new FeatureParameter<AColor>("lineColor", "Line Color", "Color of the pathfind line", new AColor(0xFFFF0000, true), TCAColor.INSTANCE));
        addParameter("lineWidth", new FeatureParameter<Float>("lineWidth", "Line Thickness", "Thickness of the pathfind line",1.0f, TCFloat.INSTANCE));
        addParameter("linerefreshrate", new FeatureParameter<Integer>("linerefreshrate", "Line Refreshrate", "Ticks to wait per line refresh. Specify it to -1 to don't refresh line at all", 10, TCInteger.INSTANCE));
        addParameter("beacon", new FeatureParameter<Boolean>("beacon", "Enable Beacons", "Enable beacons for pathfind line targets",  true, TCBoolean.INSTANCE));
        addParameter("beamColor", new FeatureParameter<AColor>("beamColor", "Beam Color", "Color of the beacon beam", new AColor(0x77FF0000, true), TCAColor.INSTANCE));
        addParameter("beamTargetColor", new FeatureParameter<AColor>("beamTargetColor", "Target Color", "Color of the target", new AColor(0x33FF0000, true), TCAColor.INSTANCE));
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
    public ActionRouteProperties getRouteProperties() {
        ActionRouteProperties actionRouteProperties = new ActionRouteProperties();
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
