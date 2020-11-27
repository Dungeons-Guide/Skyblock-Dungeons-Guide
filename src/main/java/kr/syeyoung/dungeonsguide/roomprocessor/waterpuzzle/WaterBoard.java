package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.nodes.*;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.*;
import java.util.List;

public class WaterBoard {
    WaterNode[][] board;
    RoomProcessorWaterPuzzle waterPuzzle;

    private OffsetPointSet frontPlate;
    private OffsetPointSet backPlate;
    private OffsetPointSet levers;
    private OffsetPointSet doors;
    private OffsetPoint lever;

    @Getter
    private List<SwitchData> switchData = new ArrayList<SwitchData>();
    @Getter
    private Map<String, SwitchData> validSwitches = new HashMap<String, SwitchData>();

    private WaterNodeStart waterNodeStart;
    private Map<String, WaterNodeEnd> waterNodeEndMap = new HashMap<String, WaterNodeEnd>();

    @Getter
    private Set<String> reqOpen = new HashSet<String>();

    @Getter
    private Route currentRoute;
    @Getter
    private BlockPos target;
    @Getter
    private String target2;

    public WaterBoard(RoomProcessorWaterPuzzle roomProcessorWaterPuzzle, OffsetPointSet frontPlate, OffsetPointSet backPlate, OffsetPointSet levers, OffsetPointSet doors, OffsetPoint leverMain) {
        this.waterPuzzle = roomProcessorWaterPuzzle;
        this.frontPlate = frontPlate;
        this.backPlate = backPlate;
        this.levers = levers;
        this.doors = doors;
        this.lever = leverMain;

        buildLeverStates();
        buildNodes();
    }

    private void buildLeverStates(){
        for (OffsetPoint offsetPoint : levers.getOffsetPointList()) {
            if (offsetPoint.getBlock(waterPuzzle.getDungeonRoom()) == Blocks.lever){
                BlockPos pos = offsetPoint.getBlockPos(waterPuzzle.getDungeonRoom());
                World w=  waterPuzzle.getDungeonRoom().getContext().getWorld();
                BlockLever.EnumOrientation enumOrientation = w.getBlockState(pos).getValue(BlockLever.FACING);
                EnumFacing enumFacing = enumOrientation.getFacing();
                BlockPos newPos = pos.add(enumFacing.getDirectionVec());

                int id = Block.getIdFromBlock(w.getChunkFromBlockCoords(newPos).getBlock(newPos));
                int data = w.getChunkFromBlockCoords(newPos).getBlockMetadata(newPos);

                SwitchData sw;
                switchData.add(sw = new SwitchData(pos,newPos,id+":"+data));
                validSwitches.put(id+":"+data, sw);
            }
        }
        SwitchData sw;
        switchData.add(sw = new SwitchData(lever.getBlockPos(waterPuzzle.getDungeonRoom()),lever.getBlockPos(waterPuzzle.getDungeonRoom()).add(0,-1,0),"mainStream"));
        validSwitches.put("mainStream", sw);
    }

    public void tick() {
        Set<String> doorsToOpen = new HashSet<String>();
        for (OffsetPoint offsetPoint : doors.getOffsetPointList()) {
            Block b =offsetPoint.getBlock(waterPuzzle.getDungeonRoom());
            if (b != Blocks.air) {
                doorsToOpen.add(Block.getIdFromBlock(b)+":"+offsetPoint.getData(waterPuzzle.getDungeonRoom()));
            }
        }

        if (!(reqOpen.containsAll(doorsToOpen) && doorsToOpen.containsAll(reqOpen))) {
            reqOpen = doorsToOpen;
            if (doorsToOpen.size() != 0) {
                WaterNodeEnd end = waterNodeEndMap.get(doorsToOpen.iterator().next());
                target = end.getBlockPos();
                target2 = end.getResultId();
                currentRoute = pathFind(end);
            }
        }
    }

    private final Set<Point> possibleDir = Sets.newHashSet(new Point(0,-1), new Point(1,0), new Point(-1, 0));
    public Route pathFind(WaterNodeEnd endNode) {
        Route start = new Route();
        start.setX(endNode.getX());
        start.setY(endNode.getY());
        start.getNodes().add(endNode);
        Queue<Route> routes = new LinkedList<Route>();
        routes.add(start);
        List<Route> reachedStart = new ArrayList<Route>();
        while (!routes.isEmpty()) {
            Route r2 = routes.poll();
            int x = r2.getX();
            int y = r2.getY();
            int size = 0;
            for (Point vec:possibleDir) {
                WaterNode node = getNodeAt(x + vec.x, y + vec.y);
                if (node == null) continue;
                if (r2.getNodes().contains(node)) continue;;
                if (!node.canWaterGoThrough()) continue;
                size ++;
                Route r = r2.clone();
                r.getNodes().add(node);
                r.getConditionList().add(node.getCondition());
                r.setX(node.getX());
                r.setY(node.getY());

                WaterNode void2 = getNodeAt(r.getX(), r.getY() + 1);
                if ((void2 == null || void2.getCondition() == null) && !r.getNodes().contains(void2)) {
                    continue;
                }

                if (checkContradiction(r.getConditionList())) {
                    continue;
                }

                if (node instanceof WaterNodeStart) {
                    reachedStart.add(r);
                } else {
                    routes.add(r);
                }
            }
        }
        Iterator<Route> routeIter = reachedStart.iterator();
        while (routeIter.hasNext()) {
            Route route = routeIter.next();

            addRouteConditions(route);
            if (checkContradiction(route.getConditionList()))
                routeIter.remove();
        }

        return reachedStart.get(0);
    }

    public void addRouteConditions(Route r) {
        int prevY = 0;
        int startX = -1;
        for (WaterNode node : r.getNodes()) {
            int currY = node.getY();
            if (currY != prevY) {
                if (startX != -1) {
                    int offset = node.getX() - startX;
                    if (offset != 0) {
                        int start = startX + (offset > 0 ? 1 : -1);
                        int end = node.getX() + offset;
                        int y = node.getY() + 2;
                        int y2 = node.getY() + 1;

                        for (int x = start; (start < end) ? (x <= end) : (x >= end); x += (start < end) ?1:-1){
                            WaterNode node2 = getNodeAt(x, y2);
                            if (node2 == null || !node2.canWaterGoThrough()) break;
                            node2 = getNodeAt(x, y);
                            if (node2 == null || node2.getCondition() == null) {
                                r.getConditionList().add(new WaterConditionContradict());
                                return;
                            } else {
                                r.getConditionList().add(node2.getCondition().invert());
                            }
                        }
                    }
                }
                startX = node.getX();
            }
        }
    }

    public WaterNode getNodeAt(int x, int y) {
        if (x < 0 || y < 0) return null;
        if (x >= board[0].length || y >= board.length) return null;
        return board[y][x];
    }

    private void buildNodes() {
        List<OffsetPoint> frontPoints = frontPlate.getOffsetPointList();
        List<OffsetPoint> backPoints = backPlate.getOffsetPointList();

        board = new WaterNode[19][25];
        for (int x = 0; x < 19; x++) {
            for (int y = 0; y < 25; y++) {
                OffsetPoint front = frontPoints.get(x *25 +y);
                OffsetPoint back = backPoints.get(x * 25 +y);
                int frontId = Block.getIdFromBlock(front.getBlock(waterPuzzle.getDungeonRoom()));
                int backId = Block.getIdFromBlock(back.getBlock(waterPuzzle.getDungeonRoom()));
                int frontData = front.getData(waterPuzzle.getDungeonRoom());
                int backData = back.getData(waterPuzzle.getDungeonRoom());
                WaterNode node;
                if (validSwitches.containsKey(backId +":"+backData) && frontId == 0) {
                    String resId = backId + ":"+backData;
                    node = new WaterNodeToggleable(resId, isSwitchActive(validSwitches.get(resId)), front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y);
                } else if (validSwitches.containsKey(frontId +":"+frontData)) {
                    String resId = frontId +":"+frontData;
                    node = new WaterNodeToggleable(resId, !isSwitchActive(validSwitches.get(resId)), front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y);
                } else if (frontId == 0) {
                    if (y == 24) {
                        OffsetPoint pos;
                        if (x != 0) {
                            pos = frontPoints.get((x-1)*25+y);
                        } else {
                            pos = frontPoints.get((x+1) * 25 +y);
                        }

                        int id = Block.getIdFromBlock(pos.getBlock(waterPuzzle.getDungeonRoom()));
                        int data= pos.getData(waterPuzzle.getDungeonRoom());
                        node = new WaterNodeEnd(id+":"+data, front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y);
                        waterNodeEndMap.put(id+":"+data, (WaterNodeEnd) node);
                    } else if (y == 0) {
                        waterNodeStart = (WaterNodeStart) (node = new WaterNodeStart(front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y));
                    } else {
                        node = new WaterNodeAir(front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y);
                    }
                } else {
                    node = new WaterNodeWall(front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y);
                }
                board[y][x] =node;
            }
        }
    }

    // true if contradiction
    private boolean checkContradiction(Set<WaterCondition> conditions) {
        Map<String, Boolean> conditionMap = new HashMap<String, Boolean>();
        for (WaterCondition condition : conditions) {
            if (condition instanceof WaterConditionContradict) return true;
            if (conditionMap.containsKey(condition.getBlockId())) {
                if (conditionMap.get(condition.getBlockId()) != condition.isRequiredState())
                    return true;
            } else {
                conditionMap.put(condition.getBlockId(), condition.isRequiredState());
            }
        }
        return false;
    }

    private boolean isSwitchActive(SwitchData switchData) {
        BlockPos switch2 = switchData.getSwitchLoc();
        World w=  waterPuzzle.getDungeonRoom().getContext().getWorld();
        boolean bool = w.getBlockState(switch2).getValue(BlockLever.POWERED);
        return bool;
    }


}
