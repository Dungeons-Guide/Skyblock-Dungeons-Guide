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

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class FeatureBoxBats extends SimpleFeature implements WorldRenderListener {
    public FeatureBoxBats() {
        super("Dungeon.Mobs", "Box Bats", "Box bats in dungeons\nDoes not appear through walls", "dungeon.batbox", true);
        parameters.put("radius", new FeatureParameter<Integer>("radius", "Highlight Radius", "The maximum distance between player and bats to be boxed", 20, "integer"));
        parameters.put("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Bats", new AColor(255,0,0,50), "acolor"));
    }


    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;

        final BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        int val = this.<Integer>getParameter("radius").getValue();
        final int sq = val * val;

        List<EntityBat> skeletonList = Minecraft.getMinecraft().theWorld.getEntities(EntityBat.class, new Predicate<EntityBat>() {
            @Override
            public boolean apply(@Nullable EntityBat input) {
                if (input != null && input.isInvisible()) return false;
                return input != null && input.getDistanceSq(player) < sq;
            }
        });
        AColor c = this.<AColor>getParameter("color").getValue();
        for (EntityBat entitySkeleton : skeletonList) {
            if (!entitySkeleton.isInvisible())
            RenderUtils.highlightBox(entitySkeleton, c, partialTicks, true);
        }
    }
}
