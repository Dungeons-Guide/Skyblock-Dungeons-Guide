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

import com.google.common.base.Predicate;
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
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class FeatureBoxSkelemaster extends SimpleFeature  {
    public FeatureBoxSkelemaster() {
        super("Player & Mob", "Box Skeleton Masters", "Box skeleton masters in dungeons", "dungeon.skeletonmasterbox", true);
        addParameter("radius", new FeatureParameter<Integer>("radius", "Highlight Radius", "The maximum distance between player and skeletonmaster to be boxed", 20, TCInteger.INSTANCE));
        addParameter("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Skeleton master", new AColor(255,0,0,50), TCAColor.INSTANCE));
    }


    @DGEventHandler
    public void drawWorld(RenderWorldLastEvent event) {
        float partialTicks = event.partialTicks;
        
        if (!SkyblockStatus.isOnDungeon()) return;

        final BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        int val = this.<Integer>getParameter("radius").getValue();
        final int sq = val * val;

        List<EntityArmorStand> skeletonList = Minecraft.getMinecraft().theWorld.getEntities(EntityArmorStand.class, new Predicate<EntityArmorStand>() {
            @Override
            public boolean apply(@Nullable EntityArmorStand input) {
                if (player.distanceSq(input.getPosition()) > sq) return false;
                if (!input.getAlwaysRenderNameTag()) return false;
                return input.getName().contains("Skeleton Master");
            }
        });
        AColor c = this.<AColor>getParameter("color").getValue();
        for (EntityArmorStand entitySkeleton : skeletonList) {
            RenderUtils.highlightBox(entitySkeleton, c, partialTicks, true);
        }
    }
}
