package kr.syeyoung.dungeonsguide.dungeon;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.storage.MapData;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Set;

public class MapProcessor {

    private final DungeonContext context;

    private byte[] lastMapData;

    private Dimension unitRoomDimension;
    private Dimension doorDimension; // width: width of door, height: gap between rooms
    private Point topLeftMapPoint;

    private boolean bugged = false;

    public MapProcessor(DungeonContext context) {
        this.context = context;
    }

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1 , 0));

    private void buildMap(final byte[] mapData) {
        final Point startroom = MapUtils.findFirstColorWithIn(mapData, (byte) 30, new Rectangle(0,0,128,128));
        if (startroom == null){
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("BUGGED MAP"));
            bugged = true;
            return;
        }
        // Determine room dimension
        {
            int width = MapUtils.getWidthOfColorAt(mapData, (byte) 30, startroom);
            int height = MapUtils.getHeightOfColorAt(mapData, (byte) 30, startroom);
            unitRoomDimension = new Dimension(width, height);
        }
        // determine the gap
        {
            Point midStartRoom = new Point(startroom.x + unitRoomDimension.width / 2, startroom.y +unitRoomDimension.height / 2);
            final int halfWidth = unitRoomDimension.width / 2 + 4;
            Vector2d dir = null;
            for (Vector2d v:directions) {
                byte color = MapUtils.getMapColorAt(mapData, (int)(v.x * halfWidth +midStartRoom.x), (int)(v.y *halfWidth +midStartRoom.y));
                if (color != 0) {
                    dir = v;
                    break;
                }
            }

            if (dir == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("BUGGED MAP, no connected door found"));
                bugged = true;
                return;
            }
            Point basePoint = new Point(startroom.x, startroom.y);
            if (dir.x > 0) basePoint.x += unitRoomDimension.width;
            if (dir.x < 0) basePoint.x += -1;
            if (dir.y > 0) basePoint.y += unitRoomDimension.height;
            if (dir.y < 0) basePoint.y += -1;
            int gap = MapUtils.getLengthOfColorExtending(mapData, (byte) 0, basePoint, dir);
            Point pt = MapUtils.findFirstColorWithInNegate(mapData, (byte)0, new Rectangle(basePoint.x, basePoint.y, (int)Math.abs(dir.y) * 128, (int)Math.abs(dir.x) *128));
            if (pt == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("BUGGED MAP, can't find door"));
                bugged = true;
                return;
            }
            int doorWidth = MapUtils.getLengthOfColorExtending(mapData, MapUtils.getMapColorAt(mapData, pt.x, pt.y), pt, new Vector2d((int)Math.abs(dir.y), (int)Math.abs(dir.x)));
            doorDimension = new Dimension(doorWidth, gap);
        }
        // Determine Top Left
        {
            int x = startroom.x;
            int y = startroom.y;
            while (x > unitRoomDimension.width + doorDimension.height) x -= unitRoomDimension.width + doorDimension.height;
            while (y > unitRoomDimension.height + doorDimension.height) y -= unitRoomDimension.height + doorDimension.height;
            topLeftMapPoint = new Point(x, y);
        }

        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Found Green room:"+startroom));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Dimension:"+unitRoomDimension));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("top Left:"+topLeftMapPoint));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("door dimension:"+doorDimension));
    }
    private void processMap(byte[] mapData) {

    }

    public void tick() {
        ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(8);
        byte[] mapData;
        if (stack == null || !(stack.getItem() instanceof ItemMap)) {
            mapData = lastMapData;
        } else {
            MapData mapData1 = ((ItemMap)stack.getItem()).getMapData(stack, context.getWorld());
            mapData = mapData1.colors;
        }

        if (lastMapData == null && mapData != null) buildMap(mapData);
        processMap(mapData);

        lastMapData = mapData;
    }
}
