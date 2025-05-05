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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions;


import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleMoveSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper=false)
public class ActionMoveSpot extends AbstractActionMove {
    @Getter
    private List<PossibleMoveSpot> targets;

    public ActionMoveSpot(List<PossibleMoveSpot> target, DungeonRoom dungeonRoom) {
        super(
                ActionMove.getCenterOf(RaytraceHelper.chooseMinimalY2(target).stream()
                        .min(Comparator.comparingInt(b -> b.isBlocked() ? 1 : 0)).get()
                        .getOffsetPointSet()),
                target.stream().flatMap(a -> a.getOffsetPointSet().stream()).collect(Collectors.toList())
        );
        this.targets = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return targets.stream().flatMap(a -> a.getOffsetPointSet().stream()).anyMatch(
                a-> a.getPos(dungeonRoom).squareDistanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector()) < 0.625
        );
    }

    @Override
    public String toString() {
        return "Move\n- target: "+targets.get(0).toString();
    }

}
