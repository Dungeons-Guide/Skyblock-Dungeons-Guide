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

import kr.syeyoung.dungeonsguide.mod.config.types.FeatureTypeHandler;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
@AllArgsConstructor
public class FeatureParameter<T> {
    private String key;
    private String name;
    private String description;
    private T value;
    private T default_value;
    private FeatureTypeHandler<T> featureTypeHandler;
    private Function<FeatureParameter<T>, Widget> widgetGenerator;
    private Consumer<T> changedCallback;
    private String icon;

    public FeatureParameter(String key, String name, String description, T default_value, FeatureTypeHandler<T> converter) {
        this(key, name, description,default_value, converter, null);
    }

    public FeatureParameter(String key, String name, String description, T default_value, FeatureTypeHandler<T> converter, Consumer<T> changedCallback) {
        this.key = key;
        this.name = name;
        this.default_value = default_value;
        this.description = description;
        this.featureTypeHandler = converter;
        this.widgetGenerator = featureTypeHandler::createDefaultWidgetFor;
        if(changedCallback != null){
            this.changedCallback = changedCallback;
            changedCallback.accept(default_value);
        }
    }

    public FeatureParameter<T> setWidgetGenerator(Function<FeatureParameter<T>, Widget> widgetGenerator) {
        this.widgetGenerator = widgetGenerator;
        return this;
    }

    public FeatureParameter<T> setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public void setToDefault() {
        value = (T) featureTypeHandler.deserialize(featureTypeHandler.serialize(default_value));
    }

    public T getValue() {
        return value == null ? default_value : value;
    }

    public void setValue(T newValue){
        value = newValue;
        if(changedCallback != null){
            changedCallback.accept(value);
        }
    }
}
