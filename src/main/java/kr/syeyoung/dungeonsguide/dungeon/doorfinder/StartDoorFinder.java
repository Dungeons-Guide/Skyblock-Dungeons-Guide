package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface StartDoorFinder {
    BlockPos find(World w);
}
