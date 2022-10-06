/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class FeatureBoxStarMobs extends SimpleFeature implements WorldRenderListener {
    public FeatureBoxStarMobs() {
        super("Dungeon.Mobs", "Box Starred mobs", "Box Starred mobs in dungeons", "dungeon.starmobbox", false);
        addParameter("radius", new FeatureParameter<Integer>("radius", "Highlight Radius", "The maximum distance between player and starred mobs to be boxed", 20, "integer"));
        addParameter("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Starred mobs", new AColor(0,255,255,50), "acolor"));
    }


    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;

        final BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        int val = this.<Integer>getParameter("radius").getValue();
        final int sq = val * val;

        List<EntityArmorStand> skeletonList = Minecraft.getMinecraft().theWorld.getEntities(EntityArmorStand.class, new Predicate<EntityArmorStand>() {
            @Override
            public boolean apply(@Nullable EntityArmorStand input) {
                if (player.distanceSq(input.getPosition()) > sq) return false;
                if (!input.getAlwaysRenderNameTag()) return false;
                return input.getName().contains("✯");
            }
        });
        AColor c = this.<AColor>getParameter("color").getValue();
        for (EntityArmorStand entitySkeleton : skeletonList) {
                RenderUtils.highlightBox(entitySkeleton, c, partialTicks, true);
        }
    }
}
