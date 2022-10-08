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

package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessorLivid;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.AxisAlignedBB;


public class FeatureBoxRealLivid extends SimpleFeature implements WorldRenderListener {
    public FeatureBoxRealLivid() {
        super("Bossfight.Floor 5", "Box Real Livid", "Box Real Livid in bossfight", "bossfight.realLividBox", true);
        addParameter("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Livid", new AColor(0,255,0,150), "acolor", nval -> color = nval));
    }

    AColor color = null;

    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null) return;
        if (skyblockStatus.getContext().getBossfightProcessor() == null) return;
        if (!(skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorLivid)) return;
        EntityOtherPlayerMP playerMP = ((BossfightProcessorLivid) skyblockStatus.getContext().getBossfightProcessor()).getRealLivid();

        if (playerMP != null)
            RenderUtils.highlightBox(playerMP, AxisAlignedBB.fromBounds(-0.4,0,-0.4,0.4,1.8,0.4), color, partialTicks, true);
    }
}
