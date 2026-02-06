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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCAColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorLivid;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.events.RenderWorldEvent;


public class FeatureBoxRealLivid extends SimpleFeature {
    public FeatureBoxRealLivid() {
        super("Bossfight.Floor 5", "Box Real Livid", "Box Real Livid in bossfight", "Dungeon.Bossfight.realLividBox", true);
        addParameter("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Livid", new AColor(0,255,0,150), TCAColor.INSTANCE, nval -> color = nval));
    }

    AColor color = null;

    @DGEventHandler
    public void drawWorld(RenderWorldEvent event) {
        float partialTicks = event.getPartialTicks();
        if (!SkyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() == null) return;
        if (!(DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() instanceof BossfightProcessorLivid)) return;
        UEntityPlayer entity = ((BossfightProcessorLivid) DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor()).getRealLivid();


        if (entity != null) {
           double x = (entity.getPrevPosX() + (entity.getPosX() - entity.getPrevPosX()) * partialTicks);
           double y =  (entity.getPrevPosY() + (entity.getPosY() - entity.getPrevPosY()) * partialTicks);
           double z = (entity.getPrevPosZ() + (entity.getPosZ() - entity.getPrevPosZ()) * partialTicks);


            event.getContext().highlightBox(x, y, z, new AABB(-0.4, 0, -0.4, 0.4, 1.8, 0.4), color.getRGB(), partialTicks, true);
        }
    }
}
