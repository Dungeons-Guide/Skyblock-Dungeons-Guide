package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.Keybinds;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ActionMoveNearestAir extends AbstractAction {
    private Set<Action> preRequisite = new HashSet<Action>();
    private OffsetPoint target;

    public ActionMoveNearestAir(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return target.getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 25;
    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = target.getBlockPos(dungeonRoom);
        float distance = MathHelper.sqrt_double(pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()));
        float multiplier = distance / 120f; //mobs only render ~120 blocks away
        float scale = 0.45f * multiplier;
        scale *= 25.0 / 6.0;
        RenderUtils.drawTextAtWorld("Destination", pos.getX() + 0.5f, (float) (pos.getY() + 0.5f + scale), pos.getZ() + 0.5f, 0xFF00FF00, 1f, true, false, partialTicks);
        RenderUtils.drawTextAtWorld(String.format("%.2f",MathHelper.sqrt_double(pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition())))+"m", pos.getX() + 0.5f, pos.getY() + 0.5f - scale, pos.getZ() + 0.5f, 0xFFFFFF00, 1f, true, false, partialTicks);

        if (FeatureRegistry.SECRET_TOGGLE_KEY.isEnabled() && Keybinds.togglePathfindStatus) return;
        if (latest != null){
            List<BlockPos> poses = new ArrayList<BlockPos>();
            for (int i = 0; i < latest.getCurrentPathLength(); i++) {
                PathPoint pathPoint = latest.getPathPointFromIndex(i);
                poses.add(dungeonRoom.getMin().add(pathPoint.xCoord, pathPoint.yCoord, pathPoint.zCoord));
            }
            RenderUtils.drawLines(poses, FeatureRegistry.SECRET_BROWSE.getColor(), partialTicks, true);
        }
    }

    private int tick = -1;
    private PathEntity latest;
    @Override
    public void onTick(DungeonRoom dungeonRoom) {
        tick = (tick+1) % 10;
        if (tick == 0) {
            latest = dungeonRoom.getPathFinder().createEntityPathTo(dungeonRoom.getContext().getWorld(),
                    Minecraft.getMinecraft().thePlayer, target.getBlockPos(dungeonRoom), Integer.MAX_VALUE);
        }
    }
    @Override
    public String toString() {
        return "MoveNearestAir\n- target: "+target.toString();
    }
}
