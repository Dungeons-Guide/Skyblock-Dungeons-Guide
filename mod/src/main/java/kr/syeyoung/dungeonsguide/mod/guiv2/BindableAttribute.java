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

package kr.syeyoung.dungeonsguide.mod.guiv2;

import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;

public class BindableAttribute<T> {
    public BindableAttribute(Class<T> type) {
        this.type = type;
    }
    public BindableAttribute(Class<T> type, T defaultValue) {
        this.type = type;
        value = defaultValue;
    }
    @Getter
    private final Class<T> type;
    private T value;
    private List<Consumer<T>> onUpdates = new ArrayList<>();

    private boolean updating = false;
    public void setValue(T t) {
        if (updating) return;
        updating = true;
        for (Consumer<T> onUpdate : onUpdates) {
            onUpdate.accept(t);
        }
        updating = false;
        this.value = t;
    }
    public T getValue() {
        return value;
    }

    public void addOnUpdate(Consumer<T> onUpdate) {
        onUpdates.add(onUpdate);
    }
    public void removeOnUpdate(Consumer<T> onUpdate) {
        onUpdates.remove(onUpdate);
    }

    private Set<BindableAttribute<T>> linkedWith = new HashSet<>();

    public void linkTo(BindableAttribute<T> bindableAttribute) { // This method has to be called by exporting bindable attribute
        if (bindableAttribute.type != type) throw new IllegalArgumentException("Different type!!");

        this.addOnUpdate(bindableAttribute::setValue);
        bindableAttribute.addOnUpdate(this::setValue);
        linkedWith.add(bindableAttribute);

        setValue(bindableAttribute.getValue());
    }

    public void unlink(BindableAttribute<T> bindableAttribute) {
        bindableAttribute.removeOnUpdate(this::setValue);
        removeOnUpdate(bindableAttribute::setValue);
        linkedWith.remove(bindableAttribute);
    }

    public void unlinkAll() {
        Set<BindableAttribute<T>> copy = new HashSet<>(linkedWith);
        for (BindableAttribute<T> tBindableAttribute : copy) {
            unlink(tBindableAttribute);
        }
    }
}
