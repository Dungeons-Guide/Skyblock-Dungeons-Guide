/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.Nullable;


public class PredicateArmorStand implements Predicate<Entity> {

    public static final PredicateArmorStand INSTANCE = new PredicateArmorStand();

    @Override
    public boolean apply(@Nullable Entity input) {
        return input instanceof EntityArmorStand;
    }

    @Override
    public int hashCode() {
        return 0;
    }
    @Override
    public boolean equals(Object o) {
        return o == this || o != null && (o.getClass() == this.getClass());
    }
}
