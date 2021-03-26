package kr.syeyoung.dungeonsguide.features.impl.boss.terminal;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.InteractListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorNecron;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FeatureSimonSaysSolver extends SimpleFeature implements WorldRenderListener, TickListener, InteractListener {
    public FeatureSimonSaysSolver() {
        super("Bossfight","Simon Says Solver","Solver for Simon says puzzle", "bossfight.simonsays2");
    }

    private SkyblockStatus ss = e.getDungeonsGuide().getSkyblockStatus();
    private List<BlockPos> orderbuild = new ArrayList<BlockPos>();
    private LinkedList<BlockPos> orderclick = new LinkedList<BlockPos>();

    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;

        if (orderclick.size() >= 1)
            RenderUtils.highlightBlock(orderclick.get(0), new Color(0, 255 ,255, 100), partialTicks, false);
        if (orderclick.size() >= 2)
            RenderUtils.highlightBlock(orderclick.get(1), new Color(255, 170, 0, 100), partialTicks, false);
    }
    private boolean wasButton = false;
    @Override
    public void onTick() {
        DungeonContext dc = ss.getContext();
        if (dc == null) {
            wasButton = false;
            return;
        }
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron)) return;
        World w = dc.getWorld();

        if (wasButton && w.getBlockState(new BlockPos(309, 123, 291)).getBlock() == Blocks.air) {
            orderclick.clear();
            orderbuild.clear();
            wasButton = false;
        } else if (!wasButton && w.getBlockState(new BlockPos(309, 123, 291)).getBlock() == Blocks.stone_button){
            orderclick.addAll(orderbuild);
            wasButton = true;
        }


        if (!wasButton) {
            for (BlockPos allInBox : BlockPos.getAllInBox(new BlockPos(310, 123, 291), new BlockPos(310, 120, 294))) {
                if (w.getBlockState(allInBox).getBlock() == Blocks.sea_lantern && !orderbuild.contains(allInBox)) {
                    orderbuild.add(allInBox);
                }
            }
        }
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;

        DungeonContext dc = ss.getContext();
        if (dc == null) return;
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron)) return;
        World w = dc.getWorld();

        BlockPos pos = event.pos.add(1,0,0);
        if (120 <= pos.getY() && pos.getY() <= 123 && pos.getX() == 310 && 291 <= pos.getZ() && pos.getZ() <= 294) {
            if (w.getBlockState(event.pos).getBlock() != Blocks.stone_button) return;
            if (pos.equals(orderclick.peek())) {
                orderclick.poll();
            }
        }
    }
}
