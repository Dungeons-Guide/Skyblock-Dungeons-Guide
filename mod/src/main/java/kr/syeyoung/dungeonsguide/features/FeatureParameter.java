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

import kr.syeyoung.dungeonsguide.config.types.TypeConverter;
import kr.syeyoung.dungeonsguide.config.types.TypeConverterRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeatureParameter<T> {
    private String key;

    private String name;
    private String description;

    private T value;
    private T default_value;
    private String value_type;

    public FeatureParameter(String key, String name, String description, T default_value, String value_type) {
        this.key = key; this.name = name; this.default_value = default_value;
        this.description = description; this.value_type = value_type;
    }

    public void setToDefault() {
        TypeConverter<T> typeConverter = TypeConverterRegistry.getTypeConverter(getValue_type());
        value = (T) typeConverter.deserialize(typeConverter.serialize(default_value));
    }

    public T getValue() {
        return value == null ? default_value : value;
    }
}
