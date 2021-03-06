package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BossfightProcessorThorn extends GeneralBossfightProcessor {


    public BossfightProcessorThorn() {
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight").build()
        );

        w= e.getDungeonsGuide().getSkyblockStatus().getContext().getWorld();
    }
    private final Set<BlockPos> progressBar = new HashSet<BlockPos>();
    private final World w;

    private int ticksPassed = 0;

    @Override
    public void tick() {
        ticksPassed ++;
        if (ticksPassed == 20) {
            progressBar.clear();
            for (int x = -30; x <= 30; x++) {
                for (int y = -30; y <= 30; y++) {
                    BlockPos newPos = new BlockPos(205 + x, 77, 205 + y);
                    Block b = w.getBlockState(newPos).getBlock();
                    if ((b == Blocks.coal_block || b == Blocks.sea_lantern) && w.getBlockState(newPos.add(0, 1, 0)).getBlock() != Blocks.carpet)
                        progressBar.add(newPos);
                }
            }
        }
    }

    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        healths.add(new HealthData("Thorn", (int) Math.round(BossStatus.healthScale * 4),4, true));
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
            if (w.getBlockState(pos).getBlock() == Blocks.sea_lantern ) lit++;
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
                RenderUtils.highlightBlock(pos2, w.getBlockState(pos2).getBlock() == Blocks.sea_lantern ?
                            new Color(0, 255, 0, 50) : new Color(255,0,0, 50), partialTicks, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
