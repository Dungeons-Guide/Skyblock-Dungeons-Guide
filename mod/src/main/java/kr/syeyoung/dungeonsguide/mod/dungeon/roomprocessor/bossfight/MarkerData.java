/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

@Data @AllArgsConstructor
public class MarkerData {
    private String name;
    private MobType type;
    private int markerIndex;
    private int width;
    private int height;

    public enum MobType {
        BOSS, MINIBOSS, WRONG_LIVID, TERRACOTA, GOLEM, ENEMIES, ANIMALS, TERMINALS, CRYSTALS // animals, because there are ton of them
    }

    private double prevX;
    private double prevZ;
    private double currX;
    private double currZ; // this is for interpolation
    private float prevYaw;
    private float currYaw;

    public static MarkerData fromEntity(Entity entity, MobType type, int index) {
        return new MarkerData(entity.getName(), type, index, 16, 16,
                entity.prevPosX, entity.prevPosZ, entity.posX, entity.posZ, entity.prevRotationYaw, entity.rotationYaw);
    }
}
