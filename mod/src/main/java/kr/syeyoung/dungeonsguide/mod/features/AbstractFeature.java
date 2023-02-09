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

package kr.syeyoung.dungeonsguide.mod.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.DefaultConfigurePageWidget;
import kr.syeyoung.dungeonsguide.mod.config.types.FeatureTypeHandler;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public abstract class AbstractFeature implements IFeature {
    @Getter
    private final String category;
    @Getter
    private final String name;

    @Getter
    private final String description;

    @Getter
    private final String key;

    protected Map<String, FeatureParameter> parameters = new HashMap<String, FeatureParameter>();

    protected void addParameter(String name, FeatureParameter f){
        parameters.put(name, f);
    }


    protected AbstractFeature(String category, String name, String description, String key) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.key = key;
    }

    @Getter
    @Setter
    private boolean enabled = true;

    public List<FeatureParameter> getParameters() { return new ArrayList<FeatureParameter>(parameters.values()); }

    public <T> FeatureParameter<T> getParameter(String key) {
        return parameters.get(key);
    }

    public void loadConfig(JsonObject jsonObject) { // gets key, calls it
        setEnabled(jsonObject.get("$enabled").getAsBoolean());

        for (Map.Entry<String, FeatureParameter> parameterEntry : parameters.entrySet()) {
            parameterEntry.getValue().setToDefault();
            JsonElement element = jsonObject.get(parameterEntry.getKey());
            if (element == null) continue;
            FeatureTypeHandler featureTypeHandler = parameterEntry.getValue().getFeatureTypeHandler();
            parameterEntry.getValue().setValue(featureTypeHandler.deserialize(element));
        }
    }

    public JsonObject saveConfig() {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, FeatureParameter> parameterEntry : parameters.entrySet()) {
            FeatureTypeHandler featureTypeHandler =  parameterEntry.getValue().getFeatureTypeHandler();
            JsonElement obj = featureTypeHandler.serialize(parameterEntry.getValue().getValue());
            object.add(parameterEntry.getKey(), obj);
        }
        object.addProperty("$enabled", isEnabled());
        return object;
    }

    public Widget getConfigureWidget() {
        List<Widget> widgets = new LinkedList<>();
        setupConfigureWidget(widgets);
        if (widgets.isEmpty()) return null;
        return new DefaultConfigurePageWidget(widgets);
    }

    public void setupConfigureWidget(List<Widget> widgets) {
        for (FeatureParameter parameter : getParameters()) {
            if (parameter.getWidgetGenerator() == null) continue;;
            Widget widget = (Widget) parameter.getWidgetGenerator().apply(parameter);
            if (widget == null) continue;;
            widgets.add(widget);
        }
    }

    public String getIcon() {
        return null;
    }

    public void onParameterReset() {}

    public boolean isDisableable() {
        return true;
    }
}
