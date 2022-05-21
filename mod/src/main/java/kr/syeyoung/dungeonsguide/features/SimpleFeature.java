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

public class SimpleFeature extends AbstractFeature {
    protected SimpleFeature(String category, String name, String key) {
        this(category, name, name, key);
    }
    protected SimpleFeature(String category, String name, String description, String key) {
        this(category, name, description, key, true);
    }

    protected SimpleFeature(String category, String name, String description, String key, boolean enabled) {
        super(category, name, description, key);
        this.setEnabled(enabled);
    }
}
