package kr.syeyoung.dungeonsguide.dungeon;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DungeonActionManager {
    @Getter
    private static final Map<Integer , Vec3> spawnLocation = new HashMap<Integer, Vec3>();

    @Getter
    private static final List<Integer> killeds = new ArrayList<Integer>();
}
