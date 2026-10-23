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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCAColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCInteger;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;


public class FeatureBoxBats extends SimpleFeature  {
    public FeatureBoxBats() {
        super("Player & Mob", "Box Bats", "Box bats in dungeons\nDoes not appear through walls", "dungeon.batbox", true);
        addParameter("radius", new FeatureParameter<Integer>("radius", "Highlight Radius", "The maximum distance between player and bats to be boxed", 20, TCInteger.INSTANCE));
        addParameter("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Bats", new AColor(255,0,0,50), TCAColor.INSTANCE));
    }


    @DGEventHandler
    public void drawWorld(RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        
        if (!SkyblockStatus.isOnDungeon()) return;

        // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
        final BlockPos player = Minecraft.getInstance().player.blockPosition();
        int val = this.<Integer>getParameter("radius").getValue();
        final int sq = val * val;

        // the loaded chunk area around the player (D114)
        Minecraft mc = Minecraft.getInstance();
        ChunkPos center = mc.player.chunkPosition();
        int loadedRadius = mc.options.getEffectiveRenderDistance() * 16;
        AABB loaded = new AABB(center.getMinBlockX() - loadedRadius, mc.level.getMinY(), center.getMinBlockZ() - loadedRadius,
                center.getMaxBlockX() + 1 + loadedRadius, mc.level.getMaxY() + 1, center.getMaxBlockZ() + 1 + loadedRadius);
        List<Bat> skeletonList = mc.level.getEntitiesOfClass(Bat.class, loaded, new Predicate<Bat>() {
            @Override
            public boolean test(@Nullable Bat input) {
                if (input != null && input.isInvisible()) return false;
                return input != null && input.distanceToSqr(Vec3.atLowerCornerOf(player)) < sq;
            }
        });
        AColor c = this.<AColor>getParameter("color").getValue();
        for (Bat entitySkeleton : skeletonList) {
            if (!entitySkeleton.isInvisible())
                RenderUtils.highlightBox(entitySkeleton, c, partialTicks, true);
        }
    }
}
