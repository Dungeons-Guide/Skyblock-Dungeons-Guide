package kr.syeyoung.dungeonsguide.dungeon.map;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.MapProcessor;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.dungeon.events.impl.DungeonNodataEvent;
import kr.syeyoung.dungeonsguide.events.impl.DungeonContextInitializationEvent;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Set;

public class DungeonMapData {
    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0, 1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1, 0));
    private static final Set<Vector2d> door_dirs = Sets.newHashSet(new Vector2d(0, 0.5), new Vector2d(0, -0.5), new Vector2d(0.5, 0), new Vector2d(-0.5, 0));
    public Dimension unitRoomDimension;
    public boolean bugged;
    public Dimension doorDimensions;
    public Point topLeftMapPoint;
    private final DungeonContext context;
    private final Minecraft mc;
    public boolean initialized;

    public DungeonMapData(DungeonContext context, Minecraft mc) {
        this.context = context;
        this.mc = mc;
    }


    public void eat(final byte[] mapData){
        final Point firstRoom = MapUtils.findFirstColorWithIn(mapData, (byte) 30, new Rectangle(0, 0, 128, 128));
        // Determine room dimension
        int width = MapUtils.getWidthOfColorAt(mapData, (byte) 30, firstRoom);
        int height = MapUtils.getHeightOfColorAt(mapData, (byte) 30, firstRoom);
        unitRoomDimension = new Dimension(width, height);
        Vector2d doorDir = null;
        Point midfirstRoom = new Point(firstRoom.x + unitRoomDimension.width / 2, firstRoom.y + unitRoomDimension.height / 2);
        final int halfWidth = unitRoomDimension.width / 2 + 2;
        for (Vector2d v : directions) {
            byte color = MapUtils.getMapColorAt(mapData, (int) (v.x * halfWidth + midfirstRoom.x), (int) (v.y * halfWidth + midfirstRoom.y));
            if (color != 0) {
                doorDir = v;
                break;
            }
        }

        if (doorDir == null) {
            bugged = true;
            return;
        }

        Point basePoint = new Point(firstRoom.x, firstRoom.y);
        if (doorDir.x > 0) basePoint.x += unitRoomDimension.width;
        if (doorDir.x < 0) basePoint.x -= 1;
        if (doorDir.y > 0) basePoint.y += unitRoomDimension.height;
        if (doorDir.y < 0) basePoint.y -= 1;
        int gap = MapUtils.getLengthOfColorExtending(mapData, (byte) 0, basePoint, doorDir);
        Point pt = MapUtils.findFirstColorWithInNegate(mapData, (byte) 0, new Rectangle(basePoint.x, basePoint.y, (int) Math.abs(doorDir.y) * unitRoomDimension.width + 1, (int) Math.abs(doorDir.x) * unitRoomDimension.height + 1));
        if (pt == null) {
            bugged = true;
            return;
        }
        int doorWidth = MapUtils.getLengthOfColorExtending(mapData, MapUtils.getMapColorAt(mapData, pt.x, pt.y), pt, new Vector2d((int) Math.abs(doorDir.y), (int) Math.abs(doorDir.x)));
        doorDimensions = new Dimension(doorWidth, gap);

        // Determine Top Left
        int topLeftX = firstRoom.x;
        int topLeftY = firstRoom.y;
        while (topLeftX >= unitRoomDimension.width + doorDimensions.height)
            topLeftX -= unitRoomDimension.width + doorDimensions.height;
        while (topLeftY >= unitRoomDimension.height + doorDimensions.height)
            topLeftY -= unitRoomDimension.height + doorDimensions.height;
        topLeftMapPoint = new Point(topLeftX, topLeftY);
        // determine door location based on npc, and determine map min from there
        DungeonSpecificDataProvider doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
        if (doorFinder == null) {
            bugged = true;
            return;
        }
        BlockPos door = doorFinder.findDoor(mc.theWorld, DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
        if (door == null) {
            bugged = true;
            return;
        }

        DungeonsGuide.sendDebugChat(new ChatComponentText("door Pos:" + door));

        Point unitPoint = MapProcessor.mapPointToRoomPoint(firstRoom, topLeftMapPoint, unitRoomDimension, doorDimensions);
        unitPoint.translate(unitPoint.x + 1, unitPoint.y + 1);
        unitPoint.translate((int) doorDir.x, (int) doorDir.y);

        Vector2d offset = doorFinder.findDoorOffset(mc.theWorld, DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
        boolean axisMatch = doorDir.equals(offset);

        int worldX = unitPoint.x * 16;
        int worldY = unitPoint.y * 16;
        BlockPos worldMin = door.add(-worldX, 0, -worldY);
        context.setDungeonMin(worldMin);

        DungeonsGuide.sendDebugChat(new ChatComponentText("Found Green room:" + firstRoom));
        DungeonsGuide.sendDebugChat(new ChatComponentText("Axis match:" + axisMatch));
        DungeonsGuide.sendDebugChat(new ChatComponentText("World Min:" + context.getDungeonMin()));
        DungeonsGuide.sendDebugChat(new ChatComponentText("Dimension:" + unitRoomDimension));
        DungeonsGuide.sendDebugChat(new ChatComponentText("top Left:" + topLeftMapPoint));
        DungeonsGuide.sendDebugChat(new ChatComponentText("door dimension:" + doorDimensions));
        context.createEvent(new DungeonNodataEvent("MAP_PROCESSOR_INIT"));
        initialized = true;
        MinecraftForge.EVENT_BUS.post(new DungeonContextInitializationEvent());
        
        
    }

}
