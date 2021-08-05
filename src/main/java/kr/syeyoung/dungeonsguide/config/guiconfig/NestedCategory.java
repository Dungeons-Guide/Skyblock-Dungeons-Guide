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

package kr.syeyoung.dungeonsguide.config.guiconfig;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true, fluent = true)
public
class NestedCategory {
    private final String categoryFull;
    @EqualsAndHashCode.Exclude
    private String categoryName;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private NestedCategory parent;

    public NestedCategory(String categoryFull) {
        this.categoryFull = categoryFull;
        this.categoryName = categoryFull.substring(categoryFull.lastIndexOf(".") + 1);
    }

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Map<String, NestedCategory> children = new HashMap<>();

    public NestedCategory child(NestedCategory child) {
        this.children.put(child.categoryName, child);
        child.parent = this;
        return this;
    }
}
