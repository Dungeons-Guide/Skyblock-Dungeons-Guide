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

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import net.minecraft.entity.passive.*;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class FeatureHideAnimals extends SimpleFeature {
    public FeatureHideAnimals() {
        super("Bossfight.Floor 4", "Hide animals on f4", "Hide Spirit Animals on F4. \nClick on Edit for precise setting", "bossfight.hideanimals", false);
        addParameter("sheep", new FeatureParameter<Boolean>("sheep", "Hide Sheeps", "Hide Sheeps", true, "boolean", nval -> sheep = nval));
        addParameter("cow", new FeatureParameter<Boolean>("cow", "Hide Cows", "Hide Cows", true, "boolean", nval -> cow = nval));
        addParameter("chicken", new FeatureParameter<Boolean>("chicken", "Hide Chickens", "Hide Chickens", true, "boolean", nval -> chicken = nval));
        addParameter("wolf", new FeatureParameter<Boolean>("wolf", "Hide Wolves", "Hide Wolves", true, "boolean", nval -> wolf = nval));
        addParameter("rabbit", new FeatureParameter<Boolean>("rabbit", "Hide Rabbits", "Hide Rabbits", true, "boolean", nval -> rabbit = nval));
        MinecraftForge.EVENT_BUS.register(this);
    }

    boolean sheep;
    boolean cow;
    boolean chicken;
    boolean wolf;
    boolean rabbit;



    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    @SubscribeEvent
    public void onEntityRenderPre(RenderLivingEvent.Pre renderPlayerEvent) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null) return;
        if (skyblockStatus.getContext().getBossfightProcessor() == null) return;
        if (!(skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorThorn)) return;

        if (renderPlayerEvent.entity instanceof EntitySheep && sheep) {
            renderPlayerEvent.setCanceled(true);
        } else if (renderPlayerEvent.entity instanceof EntityCow && cow ) {
            renderPlayerEvent.setCanceled(true);
        } else if (renderPlayerEvent.entity instanceof EntityChicken && chicken) {
            renderPlayerEvent.setCanceled(true);
        } else if (renderPlayerEvent.entity instanceof EntityWolf && wolf) {
            renderPlayerEvent.setCanceled(true);
        } else if (renderPlayerEvent.entity instanceof EntityRabbit && rabbit) {
            renderPlayerEvent.setCanceled(true);
        }
    }
}
