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

package kr.syeyoung.dungeonsguide.features;

import com.google.common.base.Supplier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.MFeatureEdit;
import kr.syeyoung.dungeonsguide.config.guiconfig.MParameterEdit;
import kr.syeyoung.dungeonsguide.config.guiconfig.RootConfigPanel;
import kr.syeyoung.dungeonsguide.config.types.TypeConverter;
import kr.syeyoung.dungeonsguide.config.types.TypeConverterRegistry;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public abstract class AbstractFeature {
    @Getter
    private final String category;
    @Getter
    private final String name;

    @Getter
    private final String description;

    @Getter
    private final String key;

    protected Map<String, FeatureParameter> parameters = new HashMap<String, FeatureParameter>();

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
        enabled = jsonObject.get("$enabled").getAsBoolean();

        for (Map.Entry<String, FeatureParameter> parameterEntry : parameters.entrySet()) {
            parameterEntry.getValue().setToDefault();
            JsonElement element = jsonObject.get(parameterEntry.getKey());
            if (element == null) continue;
            TypeConverter typeConverter = TypeConverterRegistry.getTypeConverter(parameterEntry.getValue().getValue_type());
            parameterEntry.getValue().setValue(typeConverter.deserialize(element));
        }
    }

    public JsonObject saveConfig() {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, FeatureParameter> parameterEntry : parameters.entrySet()) {
            TypeConverter typeConverter = TypeConverterRegistry.getTypeConverter(parameterEntry.getValue().getValue_type());
            JsonElement obj = typeConverter.serialize(parameterEntry.getValue().getValue());
            object.add(parameterEntry.getKey(), obj);
        }
        object.addProperty("$enabled", isEnabled());
        return object;
    }

    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + key , new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                MFeatureEdit featureEdit = new MFeatureEdit(AbstractFeature.this, rootConfigPanel);
                for (FeatureParameter parameter: getParameters()) {
                    featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(AbstractFeature.this, parameter, rootConfigPanel));
                }
                return featureEdit;
            }
        });
        return "base." + key ;
    }

    public void onParameterReset() {}

    public boolean isDisyllable() {
        return true;
    }
}
