package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

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

import java.util.*;
import java.util.List;

public class WaterBoard {
    @Getter
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
    private Map<String, WaterNode> toggleableMap = new HashMap<String, WaterNode>();

    @Getter
    private Set<String> reqOpen = new HashSet<String>();
    @Getter
    private Route currentRoute;
    @Getter
    private List<BlockPos> target;
    @Getter
    private List<String> target2;

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
                BlockPos newPos = pos.add(-enumFacing.getDirectionVec().getX(),0,-enumFacing.getDirectionVec().getZ());

                int id = Block.getIdFromBlock(w.getChunkFromBlockCoords(newPos).getBlock(newPos));
                int data = w.getChunkFromBlockCoords(newPos).getBlockMetadata(newPos);

                SwitchData sw;
                switchData.add(sw = new SwitchData(this, pos,newPos,id+":"+data));
                validSwitches.put(id+":"+data, sw);
            }
        }
        SwitchData sw;
        switchData.add(sw = new SwitchData(this, lever.getBlockPos(waterPuzzle.getDungeonRoom()),lever.getBlockPos(waterPuzzle.getDungeonRoom()).add(0,-1,0),"mainStream"));
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
//        if (!(reqOpen.containsAll(doorsToOpen) && doorsToOpen.containsAll(reqOpen))) {
            reqOpen = doorsToOpen;
            if (doorsToOpen.size() != 0) {
                Set<WaterNodeEnd> ends = new HashSet<WaterNodeEnd>();
                for (String s : doorsToOpen) {
                    ends.add(waterNodeEndMap.get(s));
                }
                currentRoute = getBestRoute(ends);

//                {
//
//                    Set<LeverState> currentState = new HashSet<LeverState>();
//                    World w = waterPuzzle.getDungeonRoom().getContext().getWorld();
//                    for (SwitchData switchDatum : this.switchData) {
//                        currentState.add(new LeverState(switchDatum.getBlockId(), switchDatum.getCurrentState(w)));
//                    }
//                    currentRoute = simulate(currentState);
//                }

                target = new ArrayList<BlockPos>();
                target2 = new ArrayList<String>();
                if (currentRoute != null) {
                    for (WaterNodeEnd endingNode : currentRoute.getEndingNodes()) {
                        target.add(endingNode.getBlockPos());
                        target2.add(endingNode.getResultId());
                    }
                }
            }
//        }
    }

    public Route getBestRoute(Set<WaterNodeEnd> potentialEnds) {
        int totalStates = (int) Math.pow(2, validSwitches.size() - 1);
        List<SwitchData> switchData = new ArrayList<SwitchData>();
        Set<LeverState> currentState = new HashSet<LeverState>();
        World w = waterPuzzle.getDungeonRoom().getContext().getWorld();
        for (SwitchData switchDatum : this.switchData) {
            if (!switchDatum.getBlockId().equals("mainStream")) {
                switchData.add(switchDatum);
            }
            currentState.add(new LeverState(switchDatum.getBlockId(), switchDatum.getCurrentState(w)));
        }
        PriorityQueue<Route> routes = new PriorityQueue<Route>();

        for (int i = 0; i < totalStates; i++) {
            Set<LeverState> states = new HashSet<LeverState>();
            for (int i1 = 0; i1 < switchData.size(); i1++) {
                states.add(new LeverState(switchData.get(i1).getBlockId(), ((i >> i1) & 0x1) > 0));
            }
            states.add(new LeverState("mainStream", true));

            Route r = simulate(states);

            for (LeverState leverState : currentState) {
                if (!states.contains(leverState))
                    r.setStateFlops(r.getStateFlops() + 1);
            }
            for (WaterNodeEnd potentialEnd : r.getEndingNodes()) {
                if (potentialEnds.contains(potentialEnd)) {
                    r.setMatches(r.getMatches() + 1);
                } else {
                    r.setNotMatches(r.getNotMatches() + 1);
                }
            }
            if (r.getMatches() > 0)
                routes.add(r);
        }


        return routes.peek();
    }

    public Route simulate(Set<LeverState> leverStates) {
        leverStates.add(null);
        Route r = new Route();
        final Queue<WaterNode> toGoDownTo = new LinkedList<WaterNode>();
        Set<WaterNode> searched = new HashSet<WaterNode>();
        Set<LeverState> waterBlockingStates = new HashSet<LeverState>();
        World w = waterPuzzle.getDungeonRoom().getContext().getWorld();
        toGoDownTo.add(getNodeAt(waterNodeStart.getX(), waterNodeStart.getY() + 1));
        while (!toGoDownTo.isEmpty()) {
            WaterNode asd = toGoDownTo.poll();
            if (asd == null) continue;
            if (searched.contains(asd)) continue;
            searched.add(asd);

            if (asd instanceof WaterNodeEnd) {
                if (!asd.isWaterFilled(w))
                    r.getEndingNodes().add((WaterNodeEnd) asd);
                continue;
            }

            r.getNodes().add(asd);

            if (asd.isWaterFilled(w) && (
                    (getNodeAt(asd.getX() + 1, asd.getY()) != null &&  getNodeAt(asd.getX() + 1, asd.getY()).isWaterFilled(w))
                || (getNodeAt(asd.getX() - 1, asd.getY()) != null &&  getNodeAt(asd.getX() - 1, asd.getY()).isWaterFilled(w)))) {
                boolean followWater = getNodeAt(asd.getX() - 1, asd.getY()) != null && leverStates.contains(getNodeAt(asd.getX() - 1, asd.getY()).getCondition())
                        && getNodeAt(asd.getX() - 2, asd.getY()) != null && leverStates.contains(getNodeAt(asd.getX() - 2, asd.getY()).getCondition());
                for (int i = asd.getX(); i < asd.getX() + 8; i++) {
                    WaterNode nodehere = getNodeAt(i, asd.getY());
                    if (nodehere == null) break;
                    if (followWater && !nodehere.isWaterFilled(w)) break;
                    if (!nodehere.canWaterGoThrough()) break;
                    if (!leverStates.contains(nodehere.getCondition()) && !nodehere.isWaterFilled(w)) break;
                    if (nodehere.getCondition() != null && leverStates.contains(nodehere.getCondition().invert()) && nodehere.isWaterFilled(w)) waterBlockingStates.add(nodehere.getCondition().invert());
                    WaterNode down = getNodeAt(i, asd.getY() + 1);
                    if (i != asd.getX())
                        followWater = nodehere.isWaterFilled(w) && (down == null || (down.canWaterGoThrough() && leverStates.contains(down.getCondition())));
                    r.getNodes().add(nodehere);
                    if (down != null && ((down.canWaterGoThrough() && leverStates.contains(down.getCondition())) || down.isWaterFilled(w))) {
                        toGoDownTo.add(down);
                    }
                    if (down != null && down.canWaterGoThrough() && down.getCondition() != null && leverStates.contains(down.getCondition().invert())) {
                        waterBlockingStates.add(down.getCondition().invert());
                    }
                }
                followWater = getNodeAt(asd.getX()  +1, asd.getY()) != null && leverStates.contains(getNodeAt(asd.getX() + 1, asd.getY()).getCondition())
                && getNodeAt(asd.getX()  +2, asd.getY()) != null && leverStates.contains(getNodeAt(asd.getX() + 2, asd.getY()).getCondition());
                for (int i = asd.getX(); i > asd.getX() - 8; i--) {
                    WaterNode nodehere = getNodeAt(i, asd.getY());
                    if (nodehere == null) break;
                    if (followWater && !nodehere.isWaterFilled(w)) break;
                    if (!nodehere.canWaterGoThrough()) break;
                    if (!leverStates.contains(nodehere.getCondition()) && !nodehere.isWaterFilled(w)) break;
                    if (nodehere.getCondition() != null && leverStates.contains(nodehere.getCondition().invert()) && nodehere.isWaterFilled(w)) waterBlockingStates.add(nodehere.getCondition().invert());
                    WaterNode down = getNodeAt(i, asd.getY() + 1);
                    if (i != asd.getX())
                        followWater = nodehere.isWaterFilled(w) && (down == null || (down.canWaterGoThrough() && leverStates.contains(down.getCondition())));
                    r.getNodes().add(nodehere);
                    if (down != null && ((down.canWaterGoThrough() && leverStates.contains(down.getCondition())) || down.isWaterFilled(w))) {
                        toGoDownTo.add(down);
                    }
                    if (down != null && down.canWaterGoThrough() && down.getCondition() != null && leverStates.contains(down.getCondition().invert())) {
                        waterBlockingStates.add(down.getCondition().invert());
                    }
                }
            } else {
                int minDistToDropRight = 9999;
                for (int i = asd.getX(); i < asd.getX() + 8; i++) {
                    WaterNode nodehere = getNodeAt(i, asd.getY());;
                    if (nodehere == null) break;
                    if (!nodehere.canWaterGoThrough()) break;
                    if (!leverStates.contains(nodehere.getCondition()) && !nodehere.isWaterFilled(w)) break;
                    WaterNode down = getNodeAt(i, asd.getY() + 1);
                    if (down != null && ((down.canWaterGoThrough() && leverStates.contains(down.getCondition())) || down.isWaterFilled(w))) {
                        int dist = i - asd.getX();
                        if (dist < minDistToDropRight)
                            minDistToDropRight = dist;
                        break;
                    }
                }
                int minDistToDropLeft = 9999;
                for (int i = asd.getX(); i > asd.getX() - 8; i--) {
                    WaterNode nodehere = getNodeAt(i, asd.getY());
                    if (nodehere == null) break;
                    if (!nodehere.canWaterGoThrough()) break;
                    if (!leverStates.contains(nodehere.getCondition()) && !nodehere.isWaterFilled(w)) break;
                    WaterNode down = getNodeAt(i, asd.getY() + 1);
                    if (down != null && ((down.canWaterGoThrough() && leverStates.contains(down.getCondition())) || down.isWaterFilled(w))) {
                        int dist = asd.getX() - i;
                        if (dist < minDistToDropLeft)
                            minDistToDropLeft = dist;
                        break;
                    }
                }

                int min = Math.min(minDistToDropRight, minDistToDropLeft);
                if (min == 9999) continue;
                if (minDistToDropRight == min) {
                    for (int i = asd.getX(); i <= asd.getX() + minDistToDropRight; i++) {
                        WaterNode nodehere = getNodeAt(i, asd.getY());
                        if (nodehere.getCondition() != null && leverStates.contains(nodehere.getCondition().invert()) && nodehere.isWaterFilled(w)) waterBlockingStates.add(nodehere.getCondition().invert());
                        r.getNodes().add(nodehere);
                        WaterNode down = getNodeAt(i, asd.getY() + 1);
                        if (down != null && ((down.canWaterGoThrough() && leverStates.contains(down.getCondition())) || down.isWaterFilled(w))) {
                            toGoDownTo.add(down);
                        }
                        if (down != null && down.canWaterGoThrough() && down.getCondition() != null && leverStates.contains(down.getCondition().invert())) {
                            waterBlockingStates.add(down.getCondition().invert());
                        }
                    }
                }
                if (minDistToDropLeft == min) {
                    for (int i = asd.getX(); i >= asd.getX() - minDistToDropLeft; i--) {
                        WaterNode nodehere = getNodeAt(i, asd.getY());
                        if (nodehere.getCondition() != null && leverStates.contains(nodehere.getCondition().invert()) && nodehere.isWaterFilled(w)) waterBlockingStates.add(nodehere.getCondition().invert());
                        r.getNodes().add(nodehere);
                        WaterNode down = getNodeAt(i, asd.getY() + 1);
                        if (down != null && ((down.canWaterGoThrough() && leverStates.contains(down.getCondition())) || down.isWaterFilled(w))) {
                            toGoDownTo.add(down);
                        }
                        if (down != null && down.canWaterGoThrough() && down.getCondition() != null && leverStates.contains(down.getCondition().invert())) {
                            waterBlockingStates.add(down.getCondition().invert());
                        }
                    }
                }
            }
        }
        ArrayList<LeverState> state = new ArrayList<LeverState>(waterBlockingStates);
        state.remove(null);
        Collections.sort(state, new Comparator<LeverState>() {
            @Override
            public int compare(LeverState leverState, LeverState t1) {
                int var0 = toggleableMap.get(leverState.getBlockId()).getY();
                int var1 = toggleableMap.get(t1.getBlockId()).getY();
                return var0 < var1 ? -1 : (var0 == var1 ? 0 : 1);
            }
        });
        LinkedList<LeverState> states = new LinkedList<LeverState>(state);
        for (LeverState ls : leverStates) {
            if (!states.contains(ls)) {
                states.add(ls);
            }
        }
        states.remove(null);


        r.setConditionList(states);
        return r;
    }


    public WaterNode getNodeAt(int x, int y) {
        if (x < 0 || y < 0) return null;
        if (x >= board[0].length || y >= board.length) return null;
        return board[y][x];
    }

    private void buildNodes() {
        List<OffsetPoint> frontPoints = frontPlate.getOffsetPointList();
        List<OffsetPoint> backPoints = backPlate.getOffsetPointList();

        board = new WaterNode[25][19];
        for (int x = 0; x < 19; x++) {
            for (int y = 0; y < 25; y++) {
                OffsetPoint front = frontPoints.get(x *25 +y);
                OffsetPoint back = backPoints.get(x * 25 +y);
                int frontId = Block.getIdFromBlock(front.getBlock(waterPuzzle.getDungeonRoom()));
                int backId = Block.getIdFromBlock(back.getBlock(waterPuzzle.getDungeonRoom()));
                int frontData = front.getData(waterPuzzle.getDungeonRoom());
                int backData = back.getData(waterPuzzle.getDungeonRoom());
                WaterNode node;
                if (validSwitches.containsKey(backId +":"+backData)) {
                    String resId = backId + ":"+backData;
                    node = new WaterNodeToggleable(resId, isSwitchActive(validSwitches.get(resId)), front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y);

                    toggleableMap.put(resId, node);
                } else if (validSwitches.containsKey(frontId +":"+frontData)) {
                    String resId = frontId +":"+frontData;
                    node = new WaterNodeToggleable(resId, !isSwitchActive(validSwitches.get(resId)), front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y);

                    toggleableMap.put(resId, node);
                } else if (frontId == 0 || frontId == 8 || frontId == 9) {
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
                    } else if (y == 2 && x == 9) {
                        waterNodeStart = (WaterNodeStart) (node = new WaterNodeStart(front.getBlockPos(waterPuzzle.getDungeonRoom()),
                                frontId != 0 ^ isSwitchActive(validSwitches.get("mainStream")),x,y));
                    } else {
                        node = new WaterNodeAir(front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y);
                    }
                } else {
                    node = new WaterNodeWall(front.getBlockPos(waterPuzzle.getDungeonRoom()),x,y);
                }
                board[y][x] =node;
            }
        }
        toggleableMap.put("mainStream", waterNodeStart);
    }

    private boolean isSwitchActive(SwitchData switchData) {
        BlockPos switch2 = switchData.getSwitchLoc();
        World w=  waterPuzzle.getDungeonRoom().getContext().getWorld();
        boolean bool = w.getBlockState(switch2).getValue(BlockLever.POWERED);
        return bool;
    }


}
