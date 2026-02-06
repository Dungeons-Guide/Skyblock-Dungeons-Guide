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

package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.RaytraceHelper;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleMoveSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.CollisionStateCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.InstaBreakFactorCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.WorldBackedBlockMap;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSettingRegistry;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.event.events.PlayerInteractEvent;
import kr.syeyoung.modapi.event.events.RenderWorldEvent;
import kr.syeyoung.modapi.item.Item;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class FeatureAirchkDebug extends SimpleFeature {

    public FeatureAirchkDebug() {
        super("Debug", "Airchk Debug", "Toggles airchk debug", "aidebug", false);
    }

    private List<PossibleMoveSpot> spots;
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.player.getHeldItem() == null ||
                event.player.getHeldItem().getItem() != Item.GOLDEN_AXE) {
            return;
        }
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            event.setCanceled(true);
            // reset
            Vector3D vec = new Vector3D(event.pos.getX() + 0.5, event.pos.getY() + 0.5, event.pos.getZ() + 0.5);
            AABB check = new AABB(
                    vec.x - 3.1, vec.y + 1.1, vec.z -3.1,
                    vec.x + 3.1, vec.y - 3.6, vec.z + 3.1
            );

            WorldBackedBlockMap coordinateMap = new WorldBackedBlockMap(event.world, Integer.MIN_VALUE, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 255, Integer.MAX_VALUE);
            InstaBreakFactorCalculatingCoordinateMap coordinateMap1 = new InstaBreakFactorCalculatingCoordinateMap(coordinateMap, AlgorithmSettingRegistry.STANDARD_DEFAULT_ALGORITHM_SETTING);
            CollisionStateCalculatingCoordinateMap collisionStateCalculatingCoordinateMap = new CollisionStateCalculatingCoordinateMap(
                    coordinateMap, Collections.emptySet(), coordinateMap1, new RoomBounds(
                    (short) 51, event.pos.add(-32, -100, -32), event.pos.add(32, 100, 32))
            );

            this.spots = RaytraceHelper.findMovespots(event.world,
                    new VectorI3D(event.pos.getX(), event.pos.getY(), event.pos.getZ()), a -> check.isVecInside(a), 3, (x, y, z) -> collisionStateCalculatingCoordinateMap.getBlock(x,y,z).isBlocked());
            System.out.println(spots);
        } else {
//            this.spots = null;
        }
        System.out.println(event.action);
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void renderWorldLast(RenderWorldEvent event) {
        if (spots == null) return;
        int cnt = spots.size();
        int i = 0;
        for (PossibleMoveSpot spot : spots) {
            i++;
            Color c = Color.getHSBColor(
                    1.0f * i / cnt , 0.5f, 1.0f
            );


            for (OffsetVec3 offsetVec3 : spot.getOffsetPointSet()) {
                event.getContext().highlightBox(
                        new AABB(
                                offsetVec3.xCoord - 0.025f, offsetVec3.yCoord - 0.025f + 70, offsetVec3.zCoord - 0.025f,
                                offsetVec3.xCoord + 0.025f, offsetVec3.yCoord + 0.025f + 70, offsetVec3.zCoord + 0.025f
                        ),
                        c.getRGB() | 0xFF000000,
                        event.getPartialTicks(),
                        false
                );
            }
            double cx = 0, cy =0 , cz = 0;
            for (OffsetVec3 offsetVec3 : spot.getOffsetPointSet()) {
                cx += offsetVec3.xCoord;
                cy += offsetVec3.yCoord + 70;
                cz += offsetVec3.zCoord;
            }
            cx /= spot.getOffsetPointSet().size();
            cy /= spot.getOffsetPointSet().size();
            cz /= spot.getOffsetPointSet().size();
            cy += 0.2f;
            event.getContext().drawTextAtWorld(spot.getClusterId()+"/"+spot.isBlocked(), (float) cx, (float) cy, (float) cz, c.getRGB() | 0xFF000000, 0.03f, false, true, event.getPartialTicks());


        }
    }

}
