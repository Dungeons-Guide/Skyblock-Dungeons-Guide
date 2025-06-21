/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher.guiv2;

import lombok.Getter;

import java.util.*;
import java.util.function.BiConsumer;

public class BindableAttribute<T> {
    public BindableAttribute(Class<T> type) {
        this.type = type;
        initialized = false;
    }
    public BindableAttribute(Class<T> type, T defaultValue) {
        this.type = type;
        value = defaultValue;
        initialized = true;
    }

    private boolean initialized = false;
    @Getter
    private final Class<T> type;
    private T value;
    private List<BiConsumer<T,T>> onUpdates = new ArrayList<>();

    private boolean updating = false;
    public void setValue(T t) {
        if (updating) return;
        updating = true;
        T old = this.value;
        this.value = t;
        try {
            if (!Objects.equals(t, old))
                for (BiConsumer<T, T> onUpdate : onUpdates) {
                    onUpdate.accept(old, value);
                }
        } finally {
            updating = false;
            initialized = true;
        }
    }
    public T getValue() {
        return value;
    }

    public void addOnUpdate(BiConsumer<T,T> onUpdate) {
        onUpdates.add(onUpdate);
    }
    public void removeOnUpdate(BiConsumer<T,T> onUpdate) {
        onUpdates.remove(onUpdate);
    }

    private Set<BindableAttribute<T>> linkedWith = new HashSet<>();

    private void boundSet(T old, T neu) {
        setValue(neu);
    }

    public void exportTo(BindableAttribute<T> bindableAttribute) { // This method has to be called by exporting bindable attribute
        if (bindableAttribute.type != type) throw new IllegalArgumentException("Different type!!");

        this.addOnUpdate(bindableAttribute::boundSet);
        bindableAttribute.addOnUpdate(this::boundSet);
        linkedWith.add(bindableAttribute);

        if (bindableAttribute.initialized)
            setValue(bindableAttribute.getValue());
        else
            bindableAttribute.setValue(getValue());
    }

    public void unexport(BindableAttribute<T> bindableAttribute) {
        bindableAttribute.removeOnUpdate(this::boundSet);
        removeOnUpdate(bindableAttribute::boundSet);
        linkedWith.remove(bindableAttribute);
    }

    public void unexportAll() {
        Set<BindableAttribute<T>> copy = new HashSet<>(linkedWith);
        for (BindableAttribute<T> tBindableAttribute : copy) {
            unexport(tBindableAttribute);
        }
    }
}
