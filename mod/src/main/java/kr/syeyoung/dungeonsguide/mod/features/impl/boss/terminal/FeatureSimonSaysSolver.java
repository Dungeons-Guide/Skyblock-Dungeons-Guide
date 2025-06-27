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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorMasterModeNecron;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorNecron;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.event.events.ClientTickEvent;
import kr.syeyoung.modapi.event.events.PlayerInteractEvent;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UWorld;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FeatureSimonSaysSolver extends SimpleFeature {
    public FeatureSimonSaysSolver() {
        super("Bossfight.Floor 7","Simon Says Solver","Solver for Simon says device", "Dungeon.Bossfight.simonsays2");
    }

    private final List<VectorI3D> orderBuild = new ArrayList<>();
    private final LinkedList<VectorI3D> orderClick = new LinkedList<>();

    @DGEventHandler
    public void drawWorld(RenderWorldLastEvent event) {
        float partialTicks = event.partialTicks;
        if (!isEnabled()) return;
        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) {
            return;
        }
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron || dc.getBossfightProcessor() instanceof BossfightProcessorMasterModeNecron)) return;
        if (ModAPI.getAPI().getPlayer().getPositionVector().distanceSq(110,120,94) > 400) return;


        if (orderClick.size() >= 1)
            RenderUtils.highlightBlock(orderClick.get(0), new Color(0, 255 ,255, 100), partialTicks, false);
        if (orderClick.size() >= 2)
            RenderUtils.highlightBlock(orderClick.get(1), new Color(255, 170, 0, 100), partialTicks, false);
    }
    private boolean wasButton = false;
    @DGEventHandler
    public void onTick(ClientTickEvent tickEvent) {
        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) {
            wasButton = false;
            return;
        }
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron || dc.getBossfightProcessor() instanceof BossfightProcessorMasterModeNecron)) return;

        UWorld w = dc.getUworld();
        if (wasButton && w.getBlockStateAt(110,121,92).isOf(BlockType.AIR)) { // check here instead :D
            orderClick.clear();
            orderBuild.clear();
            wasButton = false;
        } else if (!wasButton && w.getBlockStateAt(110,121,92).isOf(BlockType.STONE_BUTTON)){
            orderClick.addAll(orderBuild);
            wasButton = true;
        }


        if (!wasButton) {
            for (VectorI3D allInBox : VectorI3D.getAllInBox(new VectorI3D(111, 120, 92), new VectorI3D(111, 123, 95))) {
                if (w.getBlockStateAt(allInBox).isOf(BlockType.SEA_LANTERN) && !orderBuild.contains(allInBox)) {
                    orderBuild.add(allInBox);
                }
            }
        }
    }

    @DGEventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;

        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) return;
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron || dc.getBossfightProcessor() instanceof BossfightProcessorMasterModeNecron)) return;
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        UWorld w = dc.getUworld();

        VectorI3D pos = event.pos.add(1,0,0);

        if (120 <= pos.getY() && pos.getY() <= 123 && pos.getX() == 111 && 92 <= pos.getZ() && pos.getZ() <= 95) {
            if (!w.getBlockStateAt(event.pos).isOf(BlockType.STONE_BUTTON)) return;
            if (pos.equals(orderClick.peek())) {
                orderClick.poll();
            }
        }
    }
}
