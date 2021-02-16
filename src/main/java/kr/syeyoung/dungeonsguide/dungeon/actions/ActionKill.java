package kr.syeyoung.dungeonsguide.dungeon.actions;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.dungeon.DungeonActionManager;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.awt.*;
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
        Vec3 spawn = new Vec3(target.getBlockPos(dungeonRoom));
        for (Integer killed : DungeonActionManager.getKilleds()) {
            if (DungeonActionManager.getSpawnLocation().get(killed) == null) continue;
            if (DungeonActionManager.getSpawnLocation().get(killed).squareDistanceTo(spawn) < 100) {
                return true;
            }
        }

        return killed;
    }

    private boolean killed = false;
    @Override
    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event) {
        if (killed) return;

        Vec3 spawnLoc = DungeonActionManager.getSpawnLocation().get(event.entity.getEntityId());
        if (spawnLoc == null) return;
        if (target.getBlockPos(dungeonRoom).distanceSq(spawnLoc.xCoord, spawnLoc.yCoord, spawnLoc.zCoord) > radius * radius) return;
        if (!predicate.apply(event.entity)) return;
        killed = true;
    }
    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = target.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        RenderUtils.drawTextAtWorld("Spawn", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    @Override
    public String toString() {
        return "KillEntity\n- target: "+target.toString()+"\n- radius: "+radius+"\n- predicate: "+(predicate == null ? "null" : predicate.getClass().getSimpleName());
    }
}
