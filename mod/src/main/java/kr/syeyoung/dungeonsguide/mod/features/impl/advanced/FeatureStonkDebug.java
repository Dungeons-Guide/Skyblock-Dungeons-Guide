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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleClickingSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.CollisionStateCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.InstaBreakFactorCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.WorldBackedCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSettingRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.event.events.PlayerInteractEvent;
import kr.syeyoung.modapi.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureStonkDebug extends SimpleFeature {

    public FeatureStonkDebug() {
        super("Debug", "Stonk Debug", "Toggles stonk debug", "stdebug", false);
    }

    public List<PossibleClickingSpot> spots;
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.player.getHeldItem() == null ||
                event.player.getHeldItem().getItem() != Item.GOLDEN_SHOVEL) {
            return;
        }
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            event.setCanceled(true);
            // reset
            WorldBackedCoordinateMap coordinateMap = new WorldBackedCoordinateMap((World) event.world.getWorld(), Integer.MIN_VALUE, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 255, Integer.MAX_VALUE);
            InstaBreakFactorCalculatingCoordinateMap coordinateMap1 = new InstaBreakFactorCalculatingCoordinateMap(coordinateMap, AlgorithmSettingRegistry.STANDARD_DEFAULT_ALGORITHM_SETTING);
            CollisionStateCalculatingCoordinateMap collisionStateCalculatingCoordinateMap = new CollisionStateCalculatingCoordinateMap(
                    coordinateMap, Collections.emptySet(), coordinateMap1, new RoomBounds(
                    (short) 51, event.pos.add(-32, -100, -32), event.pos.add(32, 100, 32))
            );
            this.spots =RaytraceHelper.raycast(event.world, event.pos,
                    (x,y,z) -> collisionStateCalculatingCoordinateMap.getBlock(x,y,z).isBlocked());
            System.out.println(spots);
        } else {
//            this.spots = null;
        }
        System.out.println(event.action);
    }

    public void change(DungeonMechanicState data) {
        if (data instanceof DungeonOnewayLeverState) this.spots = ((DungeonOnewayLeverState) data).getData().getLeverCache().getPrecalculatedStonk(Collections.emptySet());
        else if (data instanceof DungeonSecretChestState) this.spots = ((DungeonSecretChestState) data).getData().getSecretCache().getPrecalculatedStonk(Collections.emptySet());
        else if (data instanceof DungeonSecretDoubleChestState) this.spots = ((DungeonSecretDoubleChestState) data).getData().getSecretCache().getPrecalculatedStonk(Collections.emptySet());
        else if (data instanceof DungeonSecretEssenceState) this.spots = ((DungeonSecretEssenceState) data).getData().getSecretCache().getPrecalculatedStonk(Collections.emptySet());
        else if (data instanceof DungeonWizardCrystalState) this.spots = ((DungeonWizardCrystalState) data).getData().getSecretCache().getPrecalculatedStonk(Collections.emptySet());
        else if (data instanceof DungeonRedstoneKeyState) this.spots = ((DungeonRedstoneKeyState) data).getData().getSecretCache().getPrecalculatedStonk(Collections.emptySet());
        else if (data instanceof DungeonLeverState) this.spots = ((DungeonLeverState) data).getData().getLeverCache().getPrecalculatedStonk(Collections.emptySet());
        else if (data instanceof DungeonFakeChestTrapState) this.spots = ((DungeonFakeChestTrapState) data).getData().getChestCache().getPrecalculatedStonk(Collections.emptySet());
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
            RenderUtils.drawTextAtWorld(
                    Arrays.stream(spot.getTools())
                            .map(a -> a == null ? "null" : a.getBreakingPower()+":"+a.getHarvestLv()).collect(Collectors.joining(";"))
                    +":::"+spot.getClusterId()+"/"+spot.isStonkingReq(), (float) cx, (float) cy, (float) cz, actual.getRGB(), 0.01f, false, true, event.partialTicks);


        }

    }

}
