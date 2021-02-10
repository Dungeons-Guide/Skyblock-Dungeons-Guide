package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.HashSet;
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
        return target.getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 10;
    }
    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = target.getBlockPos(dungeonRoom);
        RenderUtils.drawTextAtWorld("Destination", pos.getX() + 0.5f, pos.getY() + 0.6f, pos.getZ() + 0.5f, 0xFF00FF00, 1f, true, false, partialTicks);
        RenderUtils.drawTextAtWorld(String.format("%.2f", MathHelper.sqrt_double(pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition())))+"m", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 1f, true, false, partialTicks);
    }

    @Override
    public String toString() {
        return "MoveNearestAir\n- target: "+target.toString();
    }
}
