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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoomProcessorBlazeSolver extends GeneralRoomProcessor {

    private boolean highToLow = false;

    private List<ArmorStand> entityList = new ArrayList<ArmorStand>();
    private List<Blaze> blazeList = new ArrayList<>();
    private ArmorStand next;
    private Blaze currentBlaze, nextBlaze;
    public RoomProcessorBlazeSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        Object highToLow = dungeonRoom.getDungeonRoomInfo().getProperties().get("order");
        if (highToLow == null) this.highToLow = false;
        else this.highToLow = (Boolean) highToLow;
    }

    @Override
    public void tick() {
        super.tick();

        DungeonRoom dungeonRoom = getDungeonRoom();
        Level w = Minecraft.getInstance().level;
        final BlockPos low = dungeonRoom.getRoomBounds().getMin();
        final BlockPos high = dungeonRoom.getRoomBounds().getMax();
        // the room's bounds (D114)
        AABB roomBox = new AABB(low.getX(), w.getMinY(), low.getZ(), high.getX() + 1, w.getMaxY() + 1, high.getZ() + 1);
        // TODO: D5 entity name stopgap (D116)
        entityList = new ArrayList<ArmorStand>(w.getEntitiesOfClass(ArmorStand.class, roomBox, input -> {
            // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
            BlockPos pos = input.blockPosition();
            return low.getX() < pos.getX() && pos.getX() < high.getX()
                    && low.getZ() < pos.getZ() && pos.getZ() < high.getZ() && TextUtils.getFormattedText(input.getName()).toLowerCase().contains("blaze");
        }));
        blazeList = new ArrayList<Blaze>(w.getEntitiesOfClass(Blaze.class, roomBox, input -> {
            // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
            BlockPos pos = input.blockPosition();
            return low.getX() < pos.getX() && pos.getX() < high.getX()
                    && low.getZ() < pos.getZ() && pos.getZ() < high.getZ();
        }));

        Comparator<ArmorStand> comparator = Comparator.comparingInt(a -> {
            // TODO: D5 entity name stopgap (D116)
            String name = TextUtils.getFormattedText(a.getName());
            String colorGone = TextUtils.stripColor(name);
            String health2 = TextUtils.keepIntegerCharactersOnly(colorGone.split("/")[1]);
            try {
                return Integer.parseInt(health2);
            } catch (Exception e) {return -1;}
        });
        if (highToLow) {
            entityList.sort(comparator.reversed());
        } else {
            entityList.sort(comparator);
        }

        if (entityList.size() > 0) {
            next = entityList.get(0);
            currentBlaze = blazeList.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(next))).orElse(null);
        } else {
            next = null;
            currentBlaze = null;
        }
        if (entityList.size() > 1) {
            ArmorStand theNextOne = entityList.get(1);
            nextBlaze = blazeList.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(theNextOne))).orElse(null);
        } else {
            nextBlaze = null;
        }
    }


    @Override
    public void drawWorld(RenderWorldEvent event) {
        super.drawWorld(event);
        if (!FeatureRegistry.SOLVER_BLAZE.isEnabled()) return;
        if (next == null) return;
        Vec3 pos = next.getEyePosition(event.partialTicks);
        RenderUtils.drawTextAtWorld("NEXT", (float)pos.x, (float)pos.y, (float)pos.z, 0xFFFF0000, 0.5f, true, false, event.partialTicks);

        for (Blaze entity : blazeList) {
            // TODO: D72, 1.8.9 masked these boxes to the blaze's shape (and a 1.1x scaled one for the border) with the stencil buffer, 26.1 draws the boxes
            RenderUtils.highlightBox(entity, new AABB(-0.8,0, -0.8, 0.8, 2, 0.8), FeatureRegistry.SOLVER_BLAZE.getBlazeColor(), event.partialTicks, false);
            if (entity == nextBlaze) {
                RenderUtils.highlightBox(entity, new AABB(-0.8,0, -0.8, 0.8, 2, 0.8), FeatureRegistry.SOLVER_BLAZE.getNextUpBlazeColor(), event.partialTicks, false);
            } else if (entity == currentBlaze)
                RenderUtils.highlightBox(entity, new AABB(-0.8,0, -0.8, 0.8, 2, 0.8), FeatureRegistry.SOLVER_BLAZE.getNextBlazeColor(), event.partialTicks, false);

            if (FeatureRegistry.SOLVER_BLAZE.<AColor>getParameter("blazeborder").getValue().getAlpha() > 0x10) {
                RenderUtils.highlightBox(entity, new AABB(-1, 0, -1, 1, 2, 1), FeatureRegistry.SOLVER_BLAZE.<AColor>getParameter("blazeborder").getValue(), event.partialTicks, false);
            }
        }
    }



    public static class Generator implements RoomProcessorGenerator<RoomProcessorBlazeSolver> {
        @Override
        public RoomProcessorBlazeSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorBlazeSolver defaultRoomProcessor = new RoomProcessorBlazeSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
