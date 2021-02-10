package kr.syeyoung.dungeonsguide.dungeon;

import lombok.Getter;
import net.minecraft.util.Vec3;

import java.util.HashMap;
import java.util.Map;

public class EntitySpawnManager {
    @Getter
    private static final Map<Integer , Vec3> spawnLocation = new HashMap<Integer, Vec3>();
}
