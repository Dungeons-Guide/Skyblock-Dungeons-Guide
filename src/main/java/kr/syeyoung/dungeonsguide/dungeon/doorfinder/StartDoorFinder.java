package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;

public interface StartDoorFinder {
    BlockPos find(World w);
    Vector2d offset(World w);
}
