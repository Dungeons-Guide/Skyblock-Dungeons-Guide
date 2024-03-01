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

package kr.syeyoung.dungeonsguide.dungeon.data;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonBreakableWall;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonTomb;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.RaytraceHelper;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PrecalculatedMoveNearest implements Serializable {
    private static final long serialVersionUID = 4182147755650845821L;
    private final List<PossibleMoveSpot>[] spots;
    @Getter
    private final List<String> dependentRouteBlocker;
    @Getter
    private final OffsetPoint target;

    public PrecalculatedMoveNearest(List<String> dependentRouteBlocker, List<PossibleMoveSpot>[] spots, OffsetPoint target) {
        this.spots = spots;
        this.dependentRouteBlocker = dependentRouteBlocker;
        this.target = target;
    }

    public List<PossibleMoveSpot> getPrecalculatedStonk(Collection<String> openBlockers) {
        int spotIdx = 0;
        for (String routeBlocker : openBlockers) {
            int idx = dependentRouteBlocker.indexOf(routeBlocker);
            if (idx != -1) spotIdx += 1 << idx;
        }

        return spots[spotIdx];
    }

    public static PrecalculatedMoveNearest createOneBat(OffsetPoint offsetPoint, DungeonRoomInfo dri) {
        List<String> calculateFor = new ArrayList<>();
        for (Map.Entry<String, DungeonMechanic> value : dri.getMechanics().entrySet()) {
            if (!(value.getValue() instanceof RouteBlocker)) continue;
            if (value.getValue() instanceof DungeonTomb) continue;
            if (value.getValue() instanceof DungeonBreakableWall) continue; // well... let's just assume they don't exist lol
            if (value.getValue() instanceof DungeonDoor) continue; // welll.... closable door is not something oyu wanna work with
            for (OffsetPoint blockedPoint : ((RouteBlocker) value.getValue()).blockedPoints()) {
                int xDiff = Math.abs(blockedPoint.getX() - offsetPoint.getX());
                int yDiff = Math.abs(blockedPoint.getY() - offsetPoint.getY());
                int zDiff = Math.abs(blockedPoint.getZ() - offsetPoint.getZ());
                if (Math.max(xDiff, Math.max(yDiff, zDiff)) <= 5) {
                    calculateFor.add(value.getKey());
                    break;
                }
            }
        }
        Vec3 vec = new Vec3(offsetPoint.getX() + 0.5, offsetPoint.getY() + 70.5, offsetPoint.getZ() + 0.5);
        List<PossibleMoveSpot>[] spots = new List[1 << calculateFor.size()];
        for (int i = 0; i < (1 << calculateFor.size()); i++) {
            List<String> included = new ArrayList<>();
            for (int i1 = 0; i1 < calculateFor.size(); i1++) {
                if (((i >> i1) & 0x1) > 0) included.add(calculateFor.get(i1));
            }

            spots[i] = RaytraceHelper.findMovespots(new DRIWorld(dri, included), new BlockPos(offsetPoint.getX(), offsetPoint.getY()+70, offsetPoint.getZ()),
                    a -> a.squareDistanceTo(vec) <= 25, 6);
        }
        return new PrecalculatedMoveNearest(calculateFor, spots, offsetPoint);
    }
    public static PrecalculatedMoveNearest createOneItem(OffsetPoint offsetPoint, DungeonRoomInfo dri) {
        List<String> calculateFor = new ArrayList<>();
        for (Map.Entry<String, DungeonMechanic> value : dri.getMechanics().entrySet()) {
            if (!(value.getValue() instanceof RouteBlocker)) continue;
            if (value.getValue() instanceof DungeonTomb) continue;
            if (value.getValue() instanceof DungeonBreakableWall) continue; // well... let's just assume they don't exist lol
//            if (value.getValue() instanceof DungeonDoor) continue; // welll.... closable door is not something oyu wanna work with
            for (OffsetPoint blockedPoint : ((RouteBlocker) value.getValue()).blockedPoints()) {
                int xDiff = Math.abs(blockedPoint.getX() - offsetPoint.getX());
                int yDiff = Math.abs(blockedPoint.getY() - offsetPoint.getY());
                int zDiff = Math.abs(blockedPoint.getZ() - offsetPoint.getZ());
                if (Math.max(xDiff, Math.max(yDiff, zDiff)) <= 5) {
                    calculateFor.add(value.getKey());
                    break;
                }
            }
        }
        Vec3 vec = new Vec3(offsetPoint.getX() + 0.5, offsetPoint.getY() + 70.5, offsetPoint.getZ() + 0.5);
        List<PossibleMoveSpot>[] spots = new List[1 << calculateFor.size()];
        AxisAlignedBB check = AxisAlignedBB.fromBounds(
                vec.xCoord - 3, vec.yCoord + 1.1, vec.zCoord -3,
                vec.xCoord + 3, vec.yCoord - 3.6, vec.zCoord + 3
        );
        for (int i = 0; i < (1 << calculateFor.size()); i++) {
            List<String> included = new ArrayList<>();
            for (int i1 = 0; i1 < calculateFor.size(); i1++) {
                if (((i >> i1) & 0x1) > 0) included.add(calculateFor.get(i1));
            }

            spots[i] = RaytraceHelper.findMovespots(new DRIWorld(dri, included), new BlockPos(offsetPoint.getX(), offsetPoint.getY()+70, offsetPoint.getZ()),
                    a -> check.isVecInside(a), 8);
        }
        return new PrecalculatedMoveNearest(calculateFor, spots, offsetPoint);
    }

    public void render(float partialTicks, DungeonRoom dungeonRoom) {
        if (EditingContext.getEditingContext() == null) return;
        int i = 0;
        List<PossibleMoveSpot> targets = getPrecalculatedStonk(dungeonRoom.getMechanics().entrySet().stream()
                .filter(a -> a.getValue() instanceof RouteBlocker)
                .filter(a -> !((RouteBlocker) a.getValue()).isBlocking(dungeonRoom)).map(a -> a.getKey()).collect(Collectors.toList()));
        for (PossibleMoveSpot spot : targets) {
            GlStateManager.disableAlpha();
            i++;
            Color c = Color.getHSBColor(
                    1.0f * i / targets.size(), 0.5f, 1.0f
            );
            Color actual;


            GlStateManager.disableAlpha();
            if (!spot.isBlocked()) {
                actual = new Color(c.getRGB() & 0xFFFFFF | 0x90000000, true);
                PossibleMoveSpot spot2 = RaytraceHelper.chooseMinimalY2(Arrays.asList(spot)).get(0);
                for (OffsetVec3 _vec3 : spot2.getOffsetPointSet()) {
                    Vec3 offsetVec3 = _vec3.getPos(dungeonRoom);
                    RenderUtils.highlightBox(
                            new AxisAlignedBB(
                                    offsetVec3.xCoord - 0.25f, offsetVec3.yCoord + 0.025f, offsetVec3.zCoord - 0.25f,
                                    offsetVec3.xCoord + 0.25f, offsetVec3.yCoord + 0.026f, offsetVec3.zCoord + 0.25f
                            ).expand(0.0030000000949949026, 0.0030000000949949026, 0.0030000000949949026),
                            actual,
                            partialTicks,
                            true
                    );
                }
            }
            actual = new Color(c.getRGB() & 0xFFFFFF | 0x10000000, true);
            for (OffsetVec3 _vec3 : spot.getOffsetPointSet()) {
                Vec3 offsetVec3 = _vec3.getPos(dungeonRoom);
                RenderUtils.highlightBox(
                        new AxisAlignedBB(
                                offsetVec3.xCoord - 0.25f, offsetVec3.yCoord - 0.025f, offsetVec3.zCoord - 0.25f,
                                offsetVec3.xCoord + 0.25f, offsetVec3.yCoord + 0.475f, offsetVec3.zCoord + 0.25f
                        ).expand(0.0030000000949949026, 0.0030000000949949026, 0.0030000000949949026),
                        actual,
                        partialTicks,
                        true
                );
            }
            double cx = 0, cy = 0, cz = 0;
            for (OffsetVec3 _offsetVec3 : spot.getOffsetPointSet()) {
                Vec3 offsetVec3 = _offsetVec3.getPos(dungeonRoom);
                cx += offsetVec3.xCoord;
                cy += offsetVec3.yCoord;
                cz += offsetVec3.zCoord;
            }
            cx /= spot.getOffsetPointSet().size();
            cy /= spot.getOffsetPointSet().size();
            cz /= spot.getOffsetPointSet().size();
            cy += 0.2f;
            RenderUtils.drawTextAtWorld(
                    spot.getClusterId() + "/" + spot.isBlocked() + " / "+spot.getOffsetPointSet().size(), (float) cx, (float) cy, (float) cz, actual.getRGB() | 0xFF000000, 0.01f, false, true, partialTicks);


            GlStateManager.enableAlpha();
        }
    }

}