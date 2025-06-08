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

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.config.types.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.classic.ClassicPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.classic.ClassicPathEngineLineProperties;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class PathfindLineProperties extends SimpleFeature {

    @Getter
    private PathfindLineProperties parent;
    public PathfindLineProperties(String category, String name, String description, String key, boolean useParent, PathfindLineProperties parent) {
        super(category, name, description, key);
        this.parent = parent;
        this.parameters = new LinkedHashMap<>();
        if (parent != null)
            addParameter("useGlobal", new FeatureParameter<Boolean>("useGlobal", "Use Global Settings instead of this", "Completely ignore these settings, then use the parent one:: '"+parent.getName()+"'",  useParent, TCBoolean.INSTANCE));
        setSetting(PathDisplayEngineSettingRegistry.getRegistration("neoroute").createConfiguration());

    }

    @Setter
    private PathDisplayEngineSetting<?> setting;

    public void setSetting(PathDisplayEngineSetting<?> setting) {
        if (getSetting() != null) {
            oldSettingCache.put(getSetting().getRegistration().getJsonName(), getSetting());
        }
        this.setting = setting;
        oldSettingCache.put(setting.getRegistration().getJsonName(), setting);
    }

    private Map<String, PathDisplayEngineSetting<?>> oldSettingCache = new HashMap<>();

    public PathDisplayEngineSetting<?> getOldSetting(String jsonName) {
        return oldSettingCache.get(jsonName);
    }

    public PathDisplayEngineSetting<?> getSetting() {
        return isGlobal() ? parent.getSetting() : setting;
    }


    public IPathDisplayEngine<?> createPathDisplayEngine(ActionRoute route) {
        return getSetting().createPathDisplayEngine(route);
    }


    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);

        if (jsonObject.has("type") && jsonObject.has("data")) {
            String str = jsonObject.get("type").getAsString();
            PathDisplayEngineSettingRegistration<?> setting = PathDisplayEngineSettingRegistry.getRegistration(str);
            PathDisplayEngineSetting<?> setting1 = setting.createConfiguration();
            setting1.deserialize(jsonObject.getAsJsonObject("data"));
            setSetting(setting1);
        }
    }

    @Override
    public JsonObject saveConfig() {
        JsonObject jsonObject = super.saveConfig();
        jsonObject.addProperty("type", setting.getRegistration().getJsonName());
        jsonObject.add("data", setting.serialize());
        return jsonObject;
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    @Override
    public Widget getConfigureWidget() {
        return new WidgetLinePropertiesEditor(this);
    }

    public boolean isGlobal() {
        if (parent == null) return false;
        return this.<Boolean>getParameter("useGlobal").getValue();
    }
}
