package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;

public interface DungeonSpecificDataProvider {
    BlockPos findDoor(World w, String dungeonName);
    Vector2d findDoorOffset(World w, String dungeonName);
}
