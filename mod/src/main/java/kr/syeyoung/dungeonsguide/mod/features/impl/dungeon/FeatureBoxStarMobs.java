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

import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCAColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCInteger;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityArmorStand;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.List;


public class FeatureBoxStarMobs extends SimpleFeature {
    public FeatureBoxStarMobs() {
        super("Player & Mob", "Box Starred mobs", "Box Starred mobs in dungeons", "dungeon.starmobbox", false);
        addParameter("radius", new FeatureParameter<Integer>("radius", "Highlight Radius", "The maximum distance between player and starred mobs to be boxed", 20, TCInteger.INSTANCE));
        addParameter("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Starred mobs", new AColor(0,255,255,50), TCAColor.INSTANCE));
    }


    @DGEventHandler
    public void drawWorld(RenderWorldLastEvent event) {
        float partialTicks = event.partialTicks;
        
        if (!SkyblockStatus.isOnDungeon()) return;

        final Vector3D player = ModAPI.getAPI().getPlayer().getPositionVector();
        int val = this.<Integer>getParameter("radius").getValue();
        final int sq = val * val;
        int r = val+2;

        List<UEntity> skeletonList = ModAPI.getAPI().getWorld().getEntitiesWithinAabb(EntityType.ARMOR_STAND,
                new AABB(player.x - r, player.y - r, player.z - r, player.x + r, player.y + r, player.z + r));

        AColor c = this.<AColor>getParameter("color").getValue();
        for (UEntity entitySkeleton : skeletonList) {
            if (!((UEntityArmorStand)entitySkeleton).getAlwaysRenderNameTag()) continue;
            if (entitySkeleton.getPositionVector().distanceSq(player) >= sq) continue;
            if (!(entitySkeleton.getName().contains("✯") && entitySkeleton.getName().contains(" "))) continue;

            RenderUtils.highlightBox(entitySkeleton, c, partialTicks, true);
        }
    }
}
