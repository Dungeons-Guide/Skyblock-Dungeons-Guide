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

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/features/impl/boss/FeatureBoxRealLivid.java
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorLivid;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
========
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorLivid;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/features/impl/boss/FeatureBoxRealLivid.java
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.AxisAlignedBB;


public class FeatureBoxRealLivid extends SimpleFeature implements WorldRenderListener {
    public FeatureBoxRealLivid() {
        super("Dungeon.Bossfight.Floor 5", "Box Real Livid", "Box Real Livid in bossfight", "Dungeon.Bossfight.realLividBox", true);
        addParameter("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Livid", new AColor(0,255,0,150), "acolor", nval -> color = nval));
    }

    AColor color = null;

    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() == null) return;
        if (!(DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() instanceof BossfightProcessorLivid)) return;
        EntityOtherPlayerMP playerMP = ((BossfightProcessorLivid) DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor()).getRealLivid();

        if (playerMP != null)
            RenderUtils.highlightBox(playerMP, AxisAlignedBB.fromBounds(-0.4,0,-0.4,0.4,1.8,0.4), color, partialTicks, true);
    }
}
