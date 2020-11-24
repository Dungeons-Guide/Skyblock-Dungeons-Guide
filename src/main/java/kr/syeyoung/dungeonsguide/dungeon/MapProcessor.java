package kr.syeyoung.dungeonsguide.dungeon;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DoorFinderRegistry;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.StartDoorFinder;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.storage.MapData;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MapProcessor {

    private final DungeonContext context;

    private byte[] lastMapData;

    private Dimension unitRoomDimension;
    private Dimension doorDimension; // width: width of door, height: gap between rooms
    private Point topLeftMapPoint;

    private boolean bugged = false;

    private List<Point> roomsFound = new ArrayList<Point>();

    private boolean axisMatch = false;

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

            Vector2d offset = doorFinder.offset(context.getWorld());
            axisMatch = doorDir.equals(offset);

            int worldX = unitPoint.x * 16;
            int worldY = unitPoint.y * 16;
            BlockPos worldMin = door.add(-worldX, 0, -worldY);
            context.setDungeonMin(worldMin);

        }

        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Found Green room:"+startroom));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Axis match:"+axisMatch));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("World Min:"+context.getDungeonMin()));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Dimension:"+unitRoomDimension));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("top Left:"+topLeftMapPoint));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("door dimension:"+doorDimension));
    }

    public Point mapPointToRoomPoint(Point mapPoint) {
        int x = (int)((mapPoint.x - topLeftMapPoint.x) / ((double)unitRoomDimension.width + doorDimension.height));
        int y = (int)((mapPoint.y - topLeftMapPoint.y) / ((double)unitRoomDimension.height + doorDimension.height));
        return new Point(x,y);
    }
    public Point roomPointToMapPoint(Point roomPoint) {
        return new Point(roomPoint.x * (unitRoomDimension.width +doorDimension.height) + topLeftMapPoint.x,
                roomPoint.y *(unitRoomDimension.height + doorDimension.height) + topLeftMapPoint.y);
    }
    public BlockPos roomPointToWorldPoint(Point roomPoint) {
        return new BlockPos(context.getDungeonMin().getX() +(roomPoint.x * 32), context.getDungeonMin().getY(), context.getDungeonMin().getZ() +(roomPoint.y *32));
    }
    public Point worldPointToRoomPoint(BlockPos worldPoint) {
        if (context.getDungeonMin() == null) return null;
        return new Point((worldPoint.getX() - context.getDungeonMin().getX()) / 32, (worldPoint.getZ() - context.getDungeonMin().getZ()) / 32);
    }

    private void processMap(byte[] mapData) {
        int height = (int)((128.0 - topLeftMapPoint.y) / (unitRoomDimension.height + doorDimension.height));
        int width = (int) ((128.0 - topLeftMapPoint.x) / (unitRoomDimension.width + doorDimension.height));
        if (MapUtils.getMapColorAt(mapData,0,0) != 0) return;
        for (int y = 0; y <= height; y++){
            for (int x = 0; x <= width; x++) {
                if (roomsFound.contains(new Point(x,y))) continue;

                Point mapPoint = roomPointToMapPoint(new Point(x,y));
                byte color = MapUtils.getMapColorAt(mapData, mapPoint.x, mapPoint.y);
                MapUtils.record(mapData, mapPoint.x, mapPoint.y, new Color(255,255,0,80));
                if (color != 0 && color != 85) {
                    MapUtils.record(mapData, mapPoint.x, mapPoint.y, new Color(0,255,255,80));
                    DungeonRoom rooms = buildRoom(mapData, new Point(x,y));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("New Map discovered! shape: "+rooms.getShape()+ " color: "+rooms.getColor()+" unitPos: "+x+","+y));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("New Map discovered! mapMin: "+rooms.getMin()));
                    StringBuilder builder = new StringBuilder();
                    for (int dy =0;dy<4;dy++) {
                        for (int dx = 0; dx < 4; dx ++) {
                            boolean isSet = ((rooms.getShape() >> (dy * 4 + dx)) & 0x1) != 0;
                            builder.append(isSet ? "O" : "X");
                        }
                        builder.append("\n");
                    }
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Shape visual: "+builder.toString()));

                    context.getDungeonRoomList().add(rooms);
                    for (Point p:rooms.getUnitPoints()) {
                        roomsFound.add(p);
                        context.getRoomMapper().put(p, rooms);
                    }
                }

            }
        }

    }

    private DungeonRoom buildRoom(byte[] mapData, Point unitPoint) {
        Queue<Point[]> toCheck = new LinkedList<Point[]>();
        toCheck.add(new Point[] {unitPoint, unitPoint}); // requestor, target
        Set<Point> checked = new HashSet<Point>();
        List<Point> ayConnected = new ArrayList<Point>();

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = 0, maxY = 0;
        while(toCheck.peek() != null) {
            Point[] check = toCheck.poll();
            if (checked.contains(check[1])) continue;
            checked.add(check[1]);

            if (checkIfConnected(mapData, check[0], check[1])) {
                ayConnected.add(check[1]);
                if (check[1].x < minX) minX = check[1].x;
                if (check[1].y < minY) minY = check[1].y;
                if (check[1].x > maxX) maxX = check[1].x;
                if (check[1].y > maxY) maxY = check[1].y;
                for (Vector2d dir: directions) {
                    Point newPt = new Point(check[1].x + (int)dir.x, check[1].y +(int)dir.y);
                    toCheck.add(new Point[]{check[1], newPt});
                }
            }
        }

        short shape = 0;
        for (Point p:ayConnected) {
            int localX = p.x - minX, localY = p.y - minY;
            shape |= 1 <<(localY *4 + localX);
        }

        Point pt2 = roomPointToMapPoint(ayConnected.get(0));
        byte unit1 = MapUtils.getMapColorAt(mapData, pt2.x, pt2.y);

        return new DungeonRoom(ayConnected, shape, unit1, roomPointToWorldPoint(new Point(minX, minY)), roomPointToWorldPoint(new Point(maxX+1, maxY+1)).add(-1, 0, -1), context);
    }

    private boolean checkIfConnected(byte[] mapData, Point unitPoint1, Point unitPoint2) {
        if (unitPoint1 == unitPoint2) return true;
        if (unitPoint1.equals(unitPoint2)) return true;


        Point high = (unitPoint2.y > unitPoint1.y) ? unitPoint2 :(unitPoint2.x > unitPoint1.x) ? unitPoint2 : unitPoint1;
        Point low = high == unitPoint2 ? unitPoint1 : unitPoint2;

        int xOff = low.x - high.x;
        int yOff = low.y - high.y;
        Point pt = roomPointToMapPoint(high);
        Point pt2 = roomPointToMapPoint(low);
        byte unit1 = MapUtils.getMapColorAt(mapData, pt.x, pt.y);
        byte unit2 = MapUtils.getMapColorAt(mapData, pt2.x, pt2.y);
        pt.translate(xOff, yOff);
        byte unit3 = MapUtils.getMapColorAt(mapData, pt.x, pt.y);

        return unit1 == unit2 && unit2 == unit3;
    }

    public void tick() {
        if (bugged) return;
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
        else if (mapData != null) processMap(mapData);

        lastMapData = mapData;
    }
}
