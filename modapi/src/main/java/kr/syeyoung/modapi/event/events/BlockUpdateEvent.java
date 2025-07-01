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

package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.data.Pair;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.event.UEvent;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public abstract class BlockUpdateEvent extends UEvent {
    @Getter @Setter
    private Set<Pair<VectorI3D, UBlockState>> updatedBlocks = new HashSet<>();

    public static class Pre extends BlockUpdateEvent {}
    public static class Post extends BlockUpdateEvent {}
}
