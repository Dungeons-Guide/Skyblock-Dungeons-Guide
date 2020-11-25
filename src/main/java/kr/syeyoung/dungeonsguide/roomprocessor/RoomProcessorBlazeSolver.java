package kr.syeyoung.dungeonsguide.roomprocessor;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoomProcessorBlazeSolver implements RoomProcessor {

    private DungeonRoom dungeonRoom;
    private boolean highToLow = false;

    private List<Entity> entityList = new ArrayList<Entity>();
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
        entityList = new ArrayList<Entity>(w.getEntities(EntityBlaze.class, new Predicate<EntityBlaze>() {
            @Override
            public boolean apply(@Nullable EntityBlaze input) {
                BlockPos pos = input.getPosition();
                return low.getX() < pos.getX() && pos.getX() < high.getX()
                        && low.getZ() < pos.getZ() && pos.getZ() < high.getZ();
            }
        }));
    }

    @Override
    public void drawScreen() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("hehe blaze solver here", 100, 100, 0xFFFFFFFF);
    }
}
