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
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderLivingEvent;


public class FeatureHideNameTags extends SimpleFeature  {
    public FeatureHideNameTags() {
        super("Player & Mob", "Hide mob nametags", "Hide mob nametags in dungeon", "dungeon.hidenametag", false);
    }


    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    @DGEventHandler
    public void onEntityRenderPre(RenderLivingEvent.Pre renderPlayerEvent) {
        
        if (!skyblockStatus.isOnDungeon()) return;

        if (renderPlayerEvent.entity instanceof EntityArmorStand) {
            EntityArmorStand armorStand = (EntityArmorStand) renderPlayerEvent.entity;
            if (armorStand.getAlwaysRenderNameTag() && armorStand.getName().contains("❤"))
                renderPlayerEvent.setCanceled(true);
        }
    }
}
