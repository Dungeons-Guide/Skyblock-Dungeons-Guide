package kr.syeyoung.dungeonsguide.roomprocessor;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoomProcessorBlazeSolver implements RoomProcessor {

    private DungeonRoom dungeonRoom;
    private boolean highToLow = false;

    private List<EntityArmorStand> entityList = new ArrayList<EntityArmorStand>();
    private EntityArmorStand next;
    public RoomProcessorBlazeSolver(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
        Object highToLow = dungeonRoom.getDungeonRoomInfo().getProperties().get("order");
        if (highToLow == null) this.highToLow = false;
        else this.highToLow = (Boolean) highToLow;
    }

    @Override
    public void tick() {
        World w = dungeonRoom.getContext().getWorld();
        final BlockPos low = dungeonRoom.getMin();
        final BlockPos high = dungeonRoom.getMax();
        entityList = new ArrayList<EntityArmorStand>(w.getEntities(EntityArmorStand.class, new Predicate<EntityArmorStand>() {
            @Override
            public boolean apply(@Nullable EntityArmorStand input) {
                BlockPos pos = input.getPosition();
                return low.getX() < pos.getX() && pos.getX() < high.getX()
                        && low.getZ() < pos.getZ() && pos.getZ() < high.getZ() && input.getName().toLowerCase().contains("blaze");
            }
        }));

        EntityArmorStand semi_target = null;
        int health = (highToLow ? Integer.MIN_VALUE : Integer.MAX_VALUE);
        for (EntityArmorStand ea : entityList) {
            String name = ea.getName();
            String colorGone = TextUtils.stripColor(name);
            String health2 = colorGone.split("/")[1];
            try {
                int heal = Integer.parseInt(health2);
                if (highToLow && heal > health) {
                    health = heal;
                    semi_target = ea;
                } else if (!highToLow && heal < health) {
                    health = heal;
                    semi_target = ea;
                }
            } catch (Exception e){}

        }

        next = semi_target;
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (next == null) return;
        Vec3 pos = next.getPositionEyes(partialTicks);
        RenderUtils.drawTextAtWorld("NEXT", (float)pos.xCoord, (float)pos.yCoord, (float)pos.zCoord, 0xFFFF0000, 3, true, false, partialTicks);
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorBlazeSolver> {
        @Override
        public RoomProcessorBlazeSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorBlazeSolver defaultRoomProcessor = new RoomProcessorBlazeSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
