package kr.syeyoung.dungeonsguide.dungeon;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DoorFinderRegistry;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.StartDoorFinder;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.storage.MapData;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Collection;
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
        Vector2d doorDir = null;
        // determine the gap
        {
            Point midStartRoom = new Point(startroom.x + unitRoomDimension.width / 2, startroom.y +unitRoomDimension.height / 2);
            final int halfWidth = unitRoomDimension.width / 2 + 2;
            for (Vector2d v:directions) {
                byte color = MapUtils.getMapColorAt(mapData, (int)(v.x * halfWidth +midStartRoom.x), (int)(v.y *halfWidth +midStartRoom.y));
                if (color != 0) {
                    doorDir = v;
                    break;
                }
            }

            if (doorDir == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("BUGGED MAP, no connected door found"));
                bugged = true;
                return;
            }
            Point basePoint = new Point(startroom.x, startroom.y);
            if (doorDir.x > 0) basePoint.x += unitRoomDimension.width;
            if (doorDir.x < 0) basePoint.x += -1;
            if (doorDir.y > 0) basePoint.y += unitRoomDimension.height;
            if (doorDir.y < 0) basePoint.y += -1;
            int gap = MapUtils.getLengthOfColorExtending(mapData, (byte) 0, basePoint, doorDir);
            Point pt = MapUtils.findFirstColorWithInNegate(mapData, (byte)0, new Rectangle(basePoint.x, basePoint.y, (int)Math.abs(doorDir.y) * unitRoomDimension.width + 1, (int)Math.abs(doorDir.x) * unitRoomDimension.height + 1));
            if (pt == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("BUGGED MAP, can't find door"));
                bugged = true;
                return;
            }
            int doorWidth = MapUtils.getLengthOfColorExtending(mapData, MapUtils.getMapColorAt(mapData, pt.x, pt.y), pt, new Vector2d((int)Math.abs(doorDir.y), (int)Math.abs(doorDir.x)));
            doorDimension = new Dimension(doorWidth, gap);
        }
        // Determine Top Left
        {
            int x = startroom.x;
            int y = startroom.y;
            while (x >= unitRoomDimension.width + doorDimension.height) x -= unitRoomDimension.width + doorDimension.height;
            while (y >= unitRoomDimension.height + doorDimension.height) y -= unitRoomDimension.height + doorDimension.height;
            topLeftMapPoint = new Point(x, y);
        }
        // determine door location based on npc, and determine map min from there
        {
            StartDoorFinder doorFinder = DoorFinderRegistry.getDoorFinder(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
            if (doorFinder == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Couldn't find door finder for :: "+DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName()));
                bugged = true;
                return;
            }
            BlockPos door = doorFinder.find(context.getWorld());
            if (door == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Couldn't find door :: "+DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName()));
                bugged = true;
                return;
            }

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("door Pos:"+door));

            Point unitPoint = mapPointToRoomPoint(startroom);
            unitPoint.translate(unitPoint.x + 1, unitPoint.y + 1);
            unitPoint.translate((int)doorDir.x, (int)doorDir.y);

            int worldX = unitPoint.x * 16;
            int worldY = unitPoint.y * 16;
            BlockPos worldMin = door.add(-worldX, 0, -worldY);
            context.setDungeonMin(worldMin);

        }

        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Found Green room:"+startroom));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Dimension:"+unitRoomDimension));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("top Left:"+topLeftMapPoint));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("door dimension:"+doorDimension));
    }

    private Point mapPointToRoomPoint(Point mapPoint) {
        int x = (int)((mapPoint.x - topLeftMapPoint.x) / ((double)unitRoomDimension.width + doorDimension.height));
        int y = (int)((mapPoint.y - topLeftMapPoint.y) / ((double)unitRoomDimension.height + doorDimension.height));
        return new Point(x,y);
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
            if (mapData1 == null) mapData = lastMapData;
            else mapData = mapData1.colors;
        }

        if (lastMapData == null && mapData != null) buildMap(mapData);
        processMap(mapData);

        lastMapData = mapData;
    }
}
