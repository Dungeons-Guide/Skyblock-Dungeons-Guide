package kr.syeyoung.dungeonsguide.dungeon;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonMapUpdateEvent;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonNodataEvent;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonRoomDiscoverEvent;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.events.DungeonContextInitializationEvent;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec4b;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.common.MinecraftForge;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MapProcessor {

    private final DungeonContext context;

    private byte[] lastMapData;

    @Getter
    private MapData lastMapData2;

    @Getter
    private BiMap<String, String> mapIconToPlayerMap = HashBiMap.create();

    @Getter
    private Dimension unitRoomDimension;
    @Getter
    private Dimension doorDimension; // width: width of door, height: gap between rooms
    @Getter
    private Point topLeftMapPoint;

    private boolean bugged = false;

    private List<Point> roomsFound = new ArrayList<Point>();

    private boolean axisMatch = false;

    @Getter
    private boolean initialized = false;

    @Getter
    private int undiscoveredRoom = 0;

    public MapProcessor(DungeonContext context) {
        this.context = context;
    }

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1 , 0));

    private int waitCnt = 0;
    private void buildMap(final byte[] mapData) {
        final Point startroom = MapUtils.findFirstColorWithIn(mapData, (byte) 30, new Rectangle(0,0,128,128));
        if (startroom == null){
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cCouldn't retrieve map data, disabling mod for this dungeon run"));
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
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cCouldn't determine door of the room, disabling mod for this dungeon run"));
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
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cCouldn't determine door of the room, disabling mod for this dungeon run"));
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
            DungeonSpecificDataProvider doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(((SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus()).getDungeonName());
            if (doorFinder == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cCouldn't find door processor for "+e.getDungeonsGuide().getSkyblockStatus().getDungeonName()));
                bugged = true;
                return;
            }
            BlockPos door = doorFinder.findDoor(context.getWorld(), e.getDungeonsGuide().getSkyblockStatus().getDungeonName());
            if (door == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cCouldn't determine door of the room, disabling mod for this dungeon run"));
                bugged = true;
                return;
            }

            e.sendDebugChat(new ChatComponentText("door Pos:"+door));

            Point unitPoint = mapPointToRoomPoint(startroom);
            unitPoint.translate(unitPoint.x + 1, unitPoint.y + 1);
            unitPoint.translate((int)doorDir.x, (int)doorDir.y);

            Vector2d offset = doorFinder.findDoorOffset(context.getWorld(), e.getDungeonsGuide().getSkyblockStatus().getDungeonName());
            axisMatch = doorDir.equals(offset);

            int worldX = unitPoint.x * 16;
            int worldY = unitPoint.y * 16;
            BlockPos worldMin = door.add(-worldX, 0, -worldY);
            context.setDungeonMin(worldMin);

        }

        e.sendDebugChat(new ChatComponentText("Found Green room:"+startroom));
        e.sendDebugChat(new ChatComponentText("Axis match:"+axisMatch));
        e.sendDebugChat(new ChatComponentText("World Min:"+context.getDungeonMin()));
        e.sendDebugChat(new ChatComponentText("Dimension:"+unitRoomDimension));
        e.sendDebugChat(new ChatComponentText("top Left:"+topLeftMapPoint));
        e.sendDebugChat(new ChatComponentText("door dimension:"+doorDimension));
        context.createEvent(new DungeonNodataEvent("MAP_PROCESSOR_INIT"));
        initialized = true;
        MinecraftForge.EVENT_BUS.post(new DungeonContextInitializationEvent());
    }

    public Point mapPointToRoomPoint(Point mapPoint) {
        int x = (int)((mapPoint.x - topLeftMapPoint.x) / ((double)unitRoomDimension.width + doorDimension.height));
        int y = (int)((mapPoint.y - topLeftMapPoint.y) / ((double)unitRoomDimension.height + doorDimension.height));
        return new Point(x,y);
    }
    public BlockPos mapPointToWorldPoint(Point mapPoint) {
        int x = (int)((mapPoint.x - topLeftMapPoint.x) / ((double)unitRoomDimension.width + doorDimension.height) * 32 + context.getDungeonMin().getX());
        int y = (int)((mapPoint.y - topLeftMapPoint.y) / ((double)unitRoomDimension.height + doorDimension.height) * 32 + context.getDungeonMin().getZ());
        return new BlockPos(x,70,y);
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
    public Point worldPointToMapPoint(Vec3 worldPoint) {
        if (context.getDungeonMin() == null) return null;
        return new Point(topLeftMapPoint.x + (int)((worldPoint.xCoord - context.getDungeonMin().getX()) / 32.0f * (unitRoomDimension.width + doorDimension.height)), topLeftMapPoint.y + (int)((worldPoint.zCoord - context.getDungeonMin().getZ()) / 32.0f * (unitRoomDimension.height + doorDimension.height)));
    }

    private void processMap(byte[] mapData) {
        int height = (int)((128.0 - topLeftMapPoint.y) / (unitRoomDimension.height + doorDimension.height));
        int width = (int) ((128.0 - topLeftMapPoint.x) / (unitRoomDimension.width + doorDimension.height));
        undiscoveredRoom = 0;
        if (MapUtils.getMapColorAt(mapData,0,0) != 0) return;
        for (int y = 0; y <= height; y++){
            for (int x = 0; x <= width; x++) {
                Point mapPoint = roomPointToMapPoint(new Point(x,y));
                byte color = MapUtils.getMapColorAt(mapData, mapPoint.x, mapPoint.y);
                MapUtils.record(mapData, mapPoint.x, mapPoint.y, new Color(255,255,0,80));
                if (roomsFound.contains(new Point(x,y))) {
                    DungeonRoom dungeonRoom = context.getRoomMapper().get(new Point(x,y));
                    if (color == 18 && dungeonRoom.getCurrentState() != DungeonRoom.RoomState.FINISHED) {
                        dungeonRoom.setCurrentState(DungeonRoom.RoomState.COMPLETE_WITHOUT_SECRETS);
                        dungeonRoom.setTotalSecrets(0);
                    } else if (color == 30) {
                        dungeonRoom.setCurrentState(DungeonRoom.RoomState.FINISHED);
                        dungeonRoom.setTotalSecrets(0);
                    } else if (dungeonRoom.getCurrentState() != DungeonRoom.RoomState.FINISHED){
                        byte centerColor = MapUtils.getMapColorAt(mapData, mapPoint.x + unitRoomDimension.width / 2, mapPoint.y + unitRoomDimension.height / 2);
                        MapUtils.record(mapData, mapPoint.x + unitRoomDimension.width / 2, mapPoint.y + unitRoomDimension.height / 2, new Color(0,255,0,80));
                        if (centerColor == 34) {
                            dungeonRoom.setCurrentState(DungeonRoom.RoomState.COMPLETE_WITHOUT_SECRETS);
                        } else if (centerColor == 30) {
                            dungeonRoom.setCurrentState(DungeonRoom.RoomState.FINISHED);
                        } else if (centerColor == 18) { // red
                            dungeonRoom.setCurrentState(DungeonRoom.RoomState.FAILED);
                        }
                    }
                    if (dungeonRoom.getTotalSecrets() == -1) {
                        if (dungeonRoom.getColor() == 82) dungeonRoom.setTotalSecrets(0);
                        else if (dungeonRoom.getColor() == 74) dungeonRoom.setTotalSecrets(0);
                        MapUtils.record(mapData, mapPoint.x, mapPoint.y +1, new Color(0,255,0,80));
                    }
                    continue;
                }

                if (color != 0 && color != 85) {
                    MapUtils.record(mapData, mapPoint.x, mapPoint.y, new Color(0,255,255,80));
                    DungeonRoom rooms = buildRoom(mapData, new Point(x,y));
                    if (rooms == null) continue;
                    context.createEvent(new DungeonRoomDiscoverEvent(rooms.getUnitPoints().get(0), rooms.getRoomMatcher().getRotation(), rooms.getMin(), rooms.getShape(),rooms.getColor(), rooms.getDungeonRoomInfo().getUuid(), rooms.getDungeonRoomInfo().getName(), rooms.getDungeonRoomInfo().getProcessorId()));
                    e.sendDebugChat(new ChatComponentText("New Map discovered! shape: "+rooms.getShape()+ " color: "+rooms.getColor()+" unitPos: "+x+","+y));
                    e.sendDebugChat(new ChatComponentText("New Map discovered! mapMin: "+rooms.getMin() + " mapMx: "+rooms.getMax()));
                    StringBuilder builder = new StringBuilder();
                    for (int dy =0;dy<4;dy++) {
                        builder.append("\n");
                        for (int dx = 0; dx < 4; dx ++) {
                            boolean isSet = ((rooms.getShape() >> (dy * 4 + dx)) & 0x1) != 0;
                            builder.append(isSet ? "O" : "X");
                        }
                    }
                    e.sendDebugChat(new ChatComponentText("Shape visual: "+builder.toString()));

                    context.getDungeonRoomList().add(rooms);
                    for (Point p:rooms.getUnitPoints()) {
                        roomsFound.add(p);
                        context.getRoomMapper().put(p, rooms);
                    }
                    if (rooms.getRoomProcessor() != null && rooms.getRoomProcessor().readGlobalChat())
                        context.getGlobalRoomProcessors().add(rooms.getRoomProcessor());
                } else if (color == 85){
                    undiscoveredRoom++;
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

        try{
            return new DungeonRoom(ayConnected, shape, unit1, roomPointToWorldPoint(new Point(minX, minY)), roomPointToWorldPoint(new Point(maxX+1, maxY+1)).add(-1, 0, -1), context);
        } catch (IllegalStateException ex) {
            e.sendDebugChat(new ChatComponentText("Failed to load room, retrying later :: "+ex.getLocalizedMessage()));
            return null;
        }
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

        return unit1 == unit2 && unit2 == unit3 && unit1 != 0;
    }

    public boolean isThereDifference(byte[] colors1, byte[] colors) {
        if (colors1 == null || colors == null) return true;
        for (int i =0; i < colors.length; i++)
            if (colors[i] != colors1[i]) return true;
        return false;
    }
    private int stabilizationTick = 0;

    public void tick() {
        if (waitCnt < 5) {
            waitCnt++;
            return;
        }
        if (bugged) return;
        ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(8);
        byte[] mapData;
        if (stack == null || !(stack.getItem() instanceof ItemMap)) {
            mapData = lastMapData;
        } else {
            MapData mapData1 = ((ItemMap)stack.getItem()).getMapData(stack, context.getWorld());
            if (mapData1 == null) mapData = lastMapData;
            else {
                mapData = mapData1.colors;
                lastMapData2 = mapData1;

                if (isThereDifference(lastMapData, mapData)) {
                    stabilizationTick =0;
                    context.createEvent(new DungeonMapUpdateEvent(mapData));
                } else {
                    stabilizationTick++;
                }

                if (stabilizationTick > 5) {
                    if (doorDimension == null) buildMap(mapData);
                    else processMap(mapData);
                }
                lastMapData = mapData;
            }

        }


        if (lastMapData2 != null && mapIconToPlayerMap.size() < context.getPlayers().size()) {
            label: for (Map.Entry<String, Vec4b> stringVec4bEntry : lastMapData2.mapDecorations.entrySet()) {
                if (mapIconToPlayerMap.containsValue(stringVec4bEntry.getKey())) continue;
                int x = stringVec4bEntry.getValue().func_176112_b() /2 + 64;
                int y = stringVec4bEntry.getValue().func_176113_c() /2 + 64;
                BlockPos mapPos = mapPointToWorldPoint(new Point(x, y));
                String potentialPlayer = null;
                for (String player : context.getPlayers()) { // check nearby players
                    if (mapIconToPlayerMap.containsKey(player)) continue;
                    EntityPlayer entityPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(player);
                    if (entityPlayer == null || entityPlayer.isInvisible()) continue;
                    BlockPos pos = entityPlayer.getPosition();
                    int dx = mapPos.getX() - pos.getX();
                    int dz = mapPos.getZ() - pos.getZ();
                    if (dx * dx + dz * dz < 100) {
                        if (potentialPlayer != null) continue label;
                        potentialPlayer = player;
                    }
                }
                if (potentialPlayer == null) continue;

                for (Map.Entry<String, Vec4b> stringVec4bEntry2 : lastMapData2.mapDecorations.entrySet()) { // check nearby markers
                    if (mapIconToPlayerMap.containsValue(stringVec4bEntry.getKey())) continue;
                    if (stringVec4bEntry.getKey().equals(stringVec4bEntry2.getKey())) continue;
                    int x2 = stringVec4bEntry2.getValue().func_176112_b() /2 + 64;
                    int y2 = stringVec4bEntry2.getValue().func_176113_c() /2 + 64;
                    int dx = x2 - x;
                    int dy = y2 - y;
                    if (dx * dx + dy * dy < 100) {
                        continue label;
                    }
                }
                mapIconToPlayerMap.put(potentialPlayer, stringVec4bEntry.getKey());
            }
        }

    }
}
