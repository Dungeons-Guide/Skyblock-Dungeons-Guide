package kr.syeyoung.dungeonsguide.dungeon;

import lombok.Getter;
import net.minecraft.world.World;

public class DungeonContext {
    @Getter
    private World world;

    public DungeonContext(World world) {
        this.world = world;
    }

    public void tick() {

    }
}
