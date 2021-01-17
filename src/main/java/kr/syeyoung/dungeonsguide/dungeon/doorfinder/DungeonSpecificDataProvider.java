package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessor;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;

public interface DungeonSpecificDataProvider {
    BlockPos findDoor(World w, String dungeonName);
    Vector2d findDoorOffset(World w, String dungeonName);

    BossfightProcessor createBossfightProcessor(World w, String dungeonName);
}