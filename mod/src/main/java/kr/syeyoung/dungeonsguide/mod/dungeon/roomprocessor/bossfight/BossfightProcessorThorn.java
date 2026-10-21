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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DGWorld;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.bossbar.BossBar;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.bossbar.BossBarManager;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.ambient.Bat;

// TODO: D5 entity name stopgap (D116), the getFormattedText(entity.getName()) reads
public class BossfightProcessorThorn extends GeneralBossfightProcessor {


    public BossfightProcessorThorn(boolean isMasterMode) {
        super(isMasterMode ? "MASTERMODE_CATACOMBS_FLOOR_FOUR" : "CATACOMBS_FLOOR_FOUR");
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight").build()
        );
        w= DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getWorld();
        this.isMasterMode = isMasterMode;
    }
    private final Set<BlockPos> progressBar = new HashSet<BlockPos>();
    private final DGWorld w;

    private int ticksPassed = 0;

    @Override
    public void tick() {
        ticksPassed ++;
        if (ticksPassed == 20) {
            progressBar.clear();
            for (int x = -30; x <= 30; x++) {
                for (int y = -30; y <= 30; y++) {
                    BlockPos newPos = new BlockPos(5 + x, 77, 5 + y);
                    BlockState b = w.getBlockState(newPos);
                    if ((LegacyBlock.COAL_BLOCK.is(b) || LegacyBlock.SEA_LANTERN.is(b)) && !LegacyBlock.CARPET.is(w.getBlockState(newPos.offset(0, 1, 0))))
                        progressBar.add(newPos);
                }
            }
        }
    }
    private boolean isMasterMode;

    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        BossBar bossBar = BossBarManager.INSTANCE.getBossBarByName("Thorn");
        float healthScale = bossBar == null ? 0 : bossBar.getProgress();
        healths.add(new HealthData("Thorn", Math.round(healthScale * (isMasterMode ? 6 : 4)),isMasterMode ? 6 : 4, true));
        return healths;
    }

    @Override
    public String getBossName() {
        return "Thorn";
    }

    public double calculatePercentage() {
        int total = progressBar.size(), lit = 0;
        if (total == 0) return 0;
        for (BlockPos pos : progressBar) {
            if (LegacyBlock.SEA_LANTERN.is(w.getBlockState(pos))) lit++;
        }

        return lit / (double)total;
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.DEBUG.isEnabled()) return;
        try {
            BlockPos pos = new BlockPos(205,77, 205);
            RenderUtils.highlightBlock(pos, new Color(0, 255, 255, 50), partialTicks, false);
            for (BlockPos pos2 : progressBar) {
                RenderUtils.highlightBlock(pos2, LegacyBlock.SEA_LANTERN.is(w.getBlockState(pos2)) ?
                            new Color(0, 255, 0, 50) : new Color(255,0,0, 50), partialTicks, false);
            }
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @Override
    public MarkerData convertToMarker(Entity entity) {
        if (entity.isInvisible()) return null;
        if (entity instanceof Bat) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.ANIMALS, 28);
        } else if (entity instanceof Sheep) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.ANIMALS, 30);
        } else if (entity instanceof Cow) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.ANIMALS, 26);
        } else if (entity instanceof Chicken) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.ANIMALS, 31);
        } else if (entity instanceof Wolf) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.ANIMALS, 29);
        } else if (entity instanceof Rabbit) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.ANIMALS, 27);
        } else if (entity instanceof Ghast) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.BOSS, 25);
        } else if (entity instanceof RemotePlayer) {
            if (TextUtils.getFormattedText(entity.getName()).equals("Spirit Bear"))
                return MarkerData.fromEntity(entity, MarkerData.MobType.MINIBOSS, 24);
        }
        return null;
    }
}
