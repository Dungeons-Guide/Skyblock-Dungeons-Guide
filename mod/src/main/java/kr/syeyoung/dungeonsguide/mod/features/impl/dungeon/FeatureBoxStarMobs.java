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
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;


public class FeatureBoxStarMobs extends SimpleFeature {
    public FeatureBoxStarMobs() {
        super("Player & Mob", "Box Starred mobs", "Box Starred mobs in dungeons", "dungeon.starmobbox", false);
        addParameter("radius", new FeatureParameter<Integer>("radius", "Highlight Radius", "The maximum distance between player and starred mobs to be boxed", 20, TCInteger.INSTANCE));
        addParameter("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Starred mobs", new AColor(0,255,255,50), TCAColor.INSTANCE));
    }


    @DGEventHandler
    public void drawWorld(RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        
        if (!SkyblockStatus.isOnDungeon()) return;

        final BlockPos player = Minecraft.getInstance().player.blockPosition();
        int val = this.<Integer>getParameter("radius").getValue();
        final int sq = val * val;

        // the loaded chunk area around the player (D114)
        Minecraft mc = Minecraft.getInstance();
        ChunkPos center = mc.player.chunkPosition();
        int loadedRadius = mc.options.getEffectiveRenderDistance() * 16;
        AABB loaded = new AABB(center.getMinBlockX() - loadedRadius, mc.level.getMinY(), center.getMinBlockZ() - loadedRadius,
                center.getMaxBlockX() + 1 + loadedRadius, mc.level.getMaxY() + 1, center.getMaxBlockZ() + 1 + loadedRadius);
        List<ArmorStand> skeletonList = mc.level.getEntitiesOfClass(ArmorStand.class, loaded, new Predicate<ArmorStand>() {
            @Override
            public boolean test(@Nullable ArmorStand input) {
                if (player.distSqr(input.blockPosition()) > sq) return false;
                if (!input.isCustomNameVisible()) return false;
                // TODO: D5 entity name stopgap (D116)
                return TextUtils.getFormattedText(input.getName()).contains("✯") && TextUtils.getFormattedText(input.getName()).contains(" ");
            }
        });
        AColor c = this.<AColor>getParameter("color").getValue();
        for (ArmorStand entitySkeleton : skeletonList) {
                RenderUtils.highlightBox(entitySkeleton, c, partialTicks, true);
        }
    }
}
