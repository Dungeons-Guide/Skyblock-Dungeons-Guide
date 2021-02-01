package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.maze;

import kr.syeyoung.dungeonsguide.Keybinds;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MazeRightProcessor extends GeneralDefuseChamberProcessor {
    public MazeRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
        center = chamber.getBlockPos(4,4,4);

        for (int x = 0; x < 9; x++) {
            for (int y =0; y< 6; y++) {
                Block b = chamber.getBlock(x,0,y).getBlock();
                BlockPos pos = chamber.getBlockPos(x,0,y);
                blockToBlockPosMap.put(b, pos);
            }
        }
    }

    private BlockPos center;
    private Map<Block, BlockPos> blockToBlockPosMap = new HashMap<Block, BlockPos>();
    @Override
    public String getName() {
        return "mazeRight";
    }


    private Block latestRequest = null;

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawTextAtWorld(latestRequest == null ? "Request not received yet" : "Requested received "+latestRequest.getLocalizedName() , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        BlockPos pos = blockToBlockPosMap.get(latestRequest);
        if (pos == null) return;
        RenderUtils.highlightBlock(pos, new Color(0,255,0,100), partialTicks, false);
    }

    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if ("e".equals(compound.getString("a"))) {
            int latestRequestid = compound.getInteger("b");
            latestRequest = Block.getBlockById(latestRequestid);
        }
    }
}
