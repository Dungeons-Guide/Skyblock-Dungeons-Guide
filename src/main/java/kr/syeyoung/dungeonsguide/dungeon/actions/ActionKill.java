package kr.syeyoung.dungeonsguide.dungeon.actions;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.dungeon.EntitySpawnManager;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.HashSet;
import java.util.Set;

@Data
public class ActionKill extends AbstractAction {
    private Set<Action> preRequisite = new HashSet<Action>();
    private OffsetPoint target;
    private Predicate<Entity> predicate = Predicates.alwaysFalse();
    private int radius;

    public ActionKill(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return killed;
    }

    private boolean killed = false;
    @Override
    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event) {
        if (killed) return;

        Vec3 spawnLoc = EntitySpawnManager.getSpawnLocation().get(event.entity.getEntityId());
        if (spawnLoc == null) return;
        if (target.getBlockPos(dungeonRoom).distanceSq(spawnLoc.xCoord, spawnLoc.yCoord, spawnLoc.zCoord) > radius * radius) return;
        if (!predicate.apply(event.entity)) return;
        killed = true;
    }

    @Override
    public String toString() {
        return "KillEntity\n- target: "+target.toString()+"\n- radius: "+radius+"\n- predicate: "+(predicate == null ? "null" : predicate.getClass().getSimpleName());
    }
}
