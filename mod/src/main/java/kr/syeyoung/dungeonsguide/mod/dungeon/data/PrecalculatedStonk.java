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

package kr.syeyoung.dungeonsguide.mod.dungeon.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.RaytraceHelper;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonBreakableWallState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonTombState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.CollisionStateCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DRIBackedBlockMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.InstaBreakFactorCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSettingRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PrecalculatedStonk {
    private final List<PossibleClickingSpot>[] spots;
    @Getter
    private final List<String> dependentRouteBlocker;
    @Getter
    private final OffsetPoint[] targets;

    public PrecalculatedStonk(
            @JsonProperty("dependentRouteBlocker") List<String> dependentRouteBlocker,
            @JsonProperty("spots") List<PossibleClickingSpot>[] spots,
            @JsonProperty("targets") OffsetPoint[] targets,
            @JsonProperty("target") OffsetPoint target) {
        this.spots = spots;
        this.dependentRouteBlocker = dependentRouteBlocker;
        if (target != null)
            this.targets = new OffsetPoint[] {target};
        else
            this.targets = targets;
    }

    public List<PossibleClickingSpot> getPrecalculatedStonk(Collection<String> openBlockers) {
        int spotIdx = 0;
        for (String routeBlocker : openBlockers) {
            int idx = dependentRouteBlocker.indexOf(routeBlocker);
            if (idx != -1) spotIdx += 1 << idx;
        }

        return spots[spotIdx];
    }

    public static PrecalculatedStonk createOne(DungeonRoomInfo dri, OffsetPoint... offsetPoint) {
        List<String> calculateFor = new ArrayList<>();

        List<String> defaultEnable = new ArrayList<>();

        for (Map.Entry<String, DungeonMechanicData> value : dri.getMechanics().entrySet()) {
            if (!(value.getValue() instanceof WorldMutatingMechanicData)) continue;
            if (value.getValue() instanceof DungeonTombState.DungeonTombData) {
                defaultEnable.add(value.getKey());
                continue;
            }
            if (value.getValue() instanceof DungeonBreakableWallState.DungeonBreakableWallData) {
                defaultEnable.add(value.getKey());
                continue; // well... let's just assume they don't exist lol
            }
//            if (value.getValue() instanceof DungeonDoorState) continue; // welll.... closable door is not something oyu wanna work with
            label: for (OffsetPoint point : offsetPoint) {
                for (OffsetPoint blockedPoint : ((WorldMutatingMechanicData) value.getValue()).blockedPoints()) {
                    int xDiff = Math.abs(blockedPoint.getX() - point.getX());
                    int yDiff = Math.abs(blockedPoint.getY() - point.getY());
                    int zDiff = Math.abs(blockedPoint.getZ() - point.getZ());
                    if (Math.max(xDiff, Math.max(yDiff, zDiff)) <= 5) {
                        calculateFor.add(value.getKey());
                        break label;
                    }
                }
            }

        }

        HashSet<VectorI3D> poses = new HashSet<>();
        for (DungeonMechanicData value : dri.getMechanics().values()) {
            if (value instanceof DungeonTombState.DungeonTombData) {
                for (OffsetPoint offsetPoint2 : ((DungeonTombState.DungeonTombData) value).blockedPoints()) {
                    poses.add(new VectorI3D(offsetPoint2.getX(), offsetPoint2.getY(), offsetPoint2.getZ()));
                }
            } else if (value instanceof DungeonBreakableWallState.DungeonBreakableWallData) {
                for (OffsetPoint offsetPoint2 : ((DungeonBreakableWallState.DungeonBreakableWallData) value).blockedPoints()) {
                    poses.add(new VectorI3D(offsetPoint2.getX(), offsetPoint2.getY(), offsetPoint2.getZ()));
                }
            }
        }

        List<PossibleClickingSpot>[] spots = new List[1 << calculateFor.size()];
        for (int i = 0; i < (1 << calculateFor.size()); i++) {
            List<String> included = new ArrayList<>();
            included.addAll(defaultEnable);
            for (int i1 = 0; i1 < calculateFor.size(); i1++) {
                if (((i >> i1) & 0x1) > 0) included.add(calculateFor.get(i1));
            }
            List<List<PossibleClickingSpot>> list = new ArrayList<>();
            for (OffsetPoint point : offsetPoint) {
                DRIBackedBlockMap driWorld = new DRIBackedBlockMap(dri, included);
                InstaBreakFactorCalculatingCoordinateMap breakFactorCalculatingCoordinateMap = new InstaBreakFactorCalculatingCoordinateMap(driWorld, AlgorithmSettingRegistry.STANDARD_DEFAULT_ALGORITHM_SETTING);
                CollisionStateCalculatingCoordinateMap collisionStateCalculatingCoordinateMap = new CollisionStateCalculatingCoordinateMap(driWorld,
                        poses,
                        breakFactorCalculatingCoordinateMap,
                        new RoomBounds(
                            dri.getShape(),
                            new VectorI3D(0,0,0),
                            new VectorI3D(dri.getWidth(), 256, dri.getLength())
                        )
                );
                list.add(RaytraceHelper.raycast(driWorld, new VectorI3D(point.getX(), point.getY()+70, point.getZ()), (x,y,z) -> collisionStateCalculatingCoordinateMap.getBlock(x,y,z).isBlocked()));
            }
            List<PossibleClickingSpot> res = list.size() == 1 ? list.get(0) : RaytraceHelper.combine(list);

            spots[i] = res;
        }
        return new PrecalculatedStonk(calculateFor, spots, offsetPoint, null);
    }

    public void render(float partialTicks, DungeonRoom dungeonRoom) {
        if (EditingContext.getEditingContext() == null) return;
        List<PossibleClickingSpot> targets = getPrecalculatedStonk(dungeonRoom.getMechanics().entrySet().stream()
                .filter(a -> a.getValue() instanceof WorldMutatingMechanicState)
                .filter(a -> !((WorldMutatingMechanicState) a.getValue()).isBlocking(dungeonRoom)).map(a -> a.getKey()).collect(Collectors.toList()));
        int i = 0;
        for (PossibleClickingSpot spot : RaytraceHelper.chooseMinimalY(targets)) {
            GlStateManager.disableAlpha();
            i++;
            Color c = Color.getHSBColor(
                    1.0f * i / targets.size(), 0.5f, 1.0f
            );
            Color actual = new Color(c.getRGB() & 0xFFFFFF | 0x90000000, true);
            for (OffsetVec3 _vec3 : spot.getOffsetPointSet()) {
                Vector3D offsetVec3 = _vec3.getPos(dungeonRoom);
                RenderUtils.highlightBox(
                        new AABB(
                                offsetVec3.x - 0.25f, offsetVec3.y + 0.025f, offsetVec3.z - 0.25f,
                                offsetVec3.x + 0.25f, offsetVec3.y + 0.026f, offsetVec3.z + 0.25f
                        ).expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026),
                        actual,
                        partialTicks,
                        true
                );
            }
        }

        i = 0;
        for (PossibleClickingSpot spot : targets) {
            GlStateManager.disableAlpha();
            i++;
            Color c = Color.getHSBColor(
                    1.0f * i / targets.size(), 0.5f, 1.0f
            );
            Color actual = new Color(c.getRGB() & 0xFFFFFF | 0x10000000, true);
            for (OffsetVec3 _vec3 : spot.getOffsetPointSet()) {
                Vector3D offsetVec3 = _vec3.getPos(dungeonRoom);
                RenderUtils.highlightBox(
                        new AABB(
                                offsetVec3.x - 0.25f, offsetVec3.y - 0.025f, offsetVec3.z - 0.25f,
                                offsetVec3.x + 0.25f, offsetVec3.y + 0.475f, offsetVec3.z + 0.25f
                        ).expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026),
                        actual,
                        partialTicks,
                        true
                );
            }

            double cx = 0, cy = 0, cz = 0;
            for (OffsetVec3 _offsetVec3 : spot.getOffsetPointSet()) {
                Vector3D offsetVec3 = _offsetVec3.getPos(dungeonRoom);
                cx += offsetVec3.x;
                cy += offsetVec3.y;
                cz += offsetVec3.z;
            }
            cx /= spot.getOffsetPointSet().size();
            cy /= spot.getOffsetPointSet().size();
            cz /= spot.getOffsetPointSet().size();
            cy += 0.2f;
            RenderUtils.drawTextAtWorld(
                    Arrays.stream(spot.getTools())
                            .map(a -> a == null ? "null" : a.getBreakingPower() + ":" + a.getHarvestLv()).collect(Collectors.joining(";"))
                            + ":::" + spot.getClusterId() + "/" + spot.isStonkingReq(), (float) cx, (float) cy, (float) cz, actual.getRGB(), 0.01f, false, true, partialTicks);


        }
    }

}
