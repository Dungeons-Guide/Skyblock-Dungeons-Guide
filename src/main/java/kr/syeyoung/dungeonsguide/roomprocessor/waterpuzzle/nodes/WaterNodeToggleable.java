package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.nodes;

import kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.LeverState;
import kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.WaterNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

@Data
@AllArgsConstructor
public class WaterNodeToggleable implements WaterNode {
    private String blockId;
    private boolean invert;

    BlockPos blockPos;

    @Override
    public boolean canWaterGoThrough() {
        return true;
    }

    @Override
    public LeverState getCondition() {
        return new LeverState(blockId, invert);
    }


    @Override
    public boolean isWaterFilled(World w) {
        Block b = w.getChunkFromBlockCoords(blockPos).getBlock(blockPos);
        return b == Blocks.water || b == Blocks.flowing_water;
    }
    private int x,y;

    public boolean isTriggered(World w) {
        Block b= w.getChunkFromBlockCoords(blockPos).getBlock(blockPos);

        return !(b == Blocks.air || b == Blocks.water || b == Blocks.flowing_water) ^ invert;
    }


    public BlockPos getBlockPos() {
        return blockPos;
    }


    public String toString() {
        return "T:"+blockId+(invert ? ":Y":":N");
    }
}
