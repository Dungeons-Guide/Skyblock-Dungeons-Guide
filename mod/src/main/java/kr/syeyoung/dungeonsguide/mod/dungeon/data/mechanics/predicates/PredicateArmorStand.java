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

package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.predicates;

import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityArmorStand;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;


public class PredicateArmorStand implements Predicate<UEntity> {

    public static final PredicateArmorStand INSTANCE = new PredicateArmorStand();


    @Override
    public boolean test(@Nullable UEntity input) {
        return input instanceof UEntityArmorStand;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "isArmorstand";
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o != null && (o.getClass() == this.getClass());
    }
}
