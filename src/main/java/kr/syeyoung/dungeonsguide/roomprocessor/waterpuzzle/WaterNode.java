package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface WaterNode {
    boolean canWaterGoThrough();

    // condition for water go
    LeverState getCondition();

    boolean isWaterFilled(World w);

    BlockPos getBlockPos();

    int getX();
    int getY();
}
