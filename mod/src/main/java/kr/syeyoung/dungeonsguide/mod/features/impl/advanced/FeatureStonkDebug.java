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

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.dungeon.data.PossibleClickingSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.RaytraceHelper;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureStonkDebug extends SimpleFeature {

    public FeatureStonkDebug() {
        super("Debug", "Stonk Debug", "Toggles stonk debug", "stdebug", false);
    }

    private List<PossibleClickingSpot> spots;
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.entityPlayer.getHeldItem() == null ||
                event.entityPlayer.getHeldItem().getItem() != Item.getItemFromBlock(Blocks.ender_chest)) {
            return;
        }
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            event.setCanceled(true);
            // reset
            this.spots = RaytraceHelper.raycast(event.world, event.pos);
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
        for (PossibleClickingSpot spot : spots) {
            i++;
            Color c = Color.getHSBColor(
                    1.0f * i / cnt , 0.5f, 1.0f
            );
            Color actual = new Color(c.getRGB(), true);


            for (OffsetVec3 offsetVec3 : spot.getOffsetPointSet()) {
                RenderUtils.highlightBox(
                        new AxisAlignedBB(
                                offsetVec3.xCoord - 0.025f, offsetVec3.yCoord - 0.025f, offsetVec3.zCoord - 0.025f,
                                offsetVec3.xCoord + 0.025f, offsetVec3.yCoord + 0.025f, offsetVec3.zCoord + 0.025f
                        ),
                        actual,
                        event.partialTicks,
                        true
                );
            }
            double cx = 0, cy =0 , cz = 0;
            for (OffsetVec3 offsetVec3 : spot.getOffsetPointSet()) {
                cx += offsetVec3.xCoord;
                cy += offsetVec3.yCoord;
                cz += offsetVec3.zCoord;
            }
            cx /= spot.getOffsetPointSet().size();
            cy /= spot.getOffsetPointSet().size();
            cz /= spot.getOffsetPointSet().size();
            cy += 0.2f;
            RenderUtils.drawTextAtWorld(
                    Arrays.stream(spot.getTools())
                            .map(a -> a == null ? "null" : a.getBreakingPower()+":"+a.getHarvestLv()).collect(Collectors.joining(";"))
                    +":::"+spot.getClusterId()+"/"+spot.isStonkingReq(), (float) cx, (float) cy, (float) cz, actual.getRGB(), 0.01f, false, true, event.partialTicks);


        }
         i = 0;
        for (PossibleClickingSpot spot : spots) {
            i++;
            Color c = Color.getHSBColor(
                    1.0f * i / cnt , 0.5f, 1.0f
            );
            Color actual = new Color(c.getRGB(), true);
           }
    }

}
