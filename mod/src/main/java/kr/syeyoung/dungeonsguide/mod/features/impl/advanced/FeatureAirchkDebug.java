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
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.event.events.PlayerInteractEvent;
import kr.syeyoung.modapi.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.awt.*;
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
            Vec3 vec = new Vec3(event.pos.getX() + 0.5, event.pos.getY() + 0.5, event.pos.getZ() + 0.5);
            AxisAlignedBB check = AxisAlignedBB.fromBounds(
                    vec.xCoord - 3.1, vec.yCoord + 1.1, vec.zCoord -3.1,
                    vec.xCoord + 3.1, vec.yCoord - 3.6, vec.zCoord + 3.1
            );

            this.spots = RaytraceHelper.findMovespots((World)event.world.getWorld(),
                    new BlockPos(event.pos.getX(), event.pos.getY(), event.pos.getZ()), a -> check.isVecInside(a), 3);
            System.out.println(spots);
        } else {
//            this.spots = null;
        }
        System.out.println(event.action);
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void renderworldLast(RenderWorldLastEvent event) {
        if (spots == null) return;
        int cnt = spots.size();
        int i = 0;
        for (PossibleMoveSpot spot : spots) {
            i++;
            Color c = Color.getHSBColor(
                    1.0f * i / cnt , 0.5f, 1.0f
            );
            Color actual = new Color(c.getRGB(), true);


            for (OffsetVec3 offsetVec3 : spot.getOffsetPointSet()) {
                RenderUtils.highlightBox(
                        new AABB(
                                offsetVec3.xCoord - 0.025f, offsetVec3.yCoord - 0.025f + 70, offsetVec3.zCoord - 0.025f,
                                offsetVec3.xCoord + 0.025f, offsetVec3.yCoord + 0.025f + 70, offsetVec3.zCoord + 0.025f
                        ),
                        actual,
                        event.partialTicks,
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
            RenderUtils.drawTextAtWorld(spot.getClusterId()+"/"+spot.isBlocked(), (float) cx, (float) cy, (float) cz, actual.getRGB(), 0.03f, false, true, event.partialTicks);


        }
    }

}
