/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessorGenerator;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class RoomProcessorWaterPuzzle extends GeneralRoomProcessor {

    private boolean argumentsFulfilled = false;

    private final OffsetPointSet doorsClosed;
    private final OffsetPointSet levers;
    private final OffsetPointSet frontBoard;
    private final OffsetPointSet backBoard;



    private Simulator.Node nodes[][];
    private Simulator.Pt waterNodeStart;
    private Map<String, Simulator.Pt> waterNodeEnds = new HashMap<>();
    private Map<String, List<Simulator.Pt>> switchFlips = new HashMap<>();
    private Map<String, BlockPos> switchLoc = new HashMap<>();
    private List<String> targetDoors = new ArrayList<>();

    private Map<Simulator.Pt, BlockPos> ptMapping = new HashMap<>();
    private List<WaterPathfinder.NodeNode> solutionList = new ArrayList<>();
    private long lastStable;
    private long lastUnstable;

    public RoomProcessorWaterPuzzle(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        frontBoard = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("front");
        backBoard = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("back");
        levers = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("levers");
        doorsClosed = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("doors");
        OffsetPoint water_lever = (OffsetPoint) dungeonRoom.getDungeonRoomInfo().getProperties().get("water-lever");

        if (frontBoard == null || backBoard == null || levers == null || doorsClosed == null ||water_lever == null) {
           argumentsFulfilled = false;
        } else {
            argumentsFulfilled = true;

            buildLeverStates();
            buildNodes(true);
            targetDoors();

        }



    }


    private void buildLeverStates(){
        for (OffsetPoint offsetPoint : levers.getOffsetPointList()) {
            if (offsetPoint.getBlock(getDungeonRoom()) == Blocks.lever){
                BlockPos pos = offsetPoint.getBlockPos(getDungeonRoom());
                World w=  getDungeonRoom().getContext().getWorld();
                BlockLever.EnumOrientation enumOrientation = w.getBlockState(pos).getValue(BlockLever.FACING);
                EnumFacing enumFacing = enumOrientation.getFacing();
                BlockPos newPos = pos.add(-enumFacing.getDirectionVec().getX(),0,-enumFacing.getDirectionVec().getZ());

                int id = Block.getIdFromBlock(w.getChunkFromBlockCoords(newPos).getBlock(newPos));
                int data = w.getChunkFromBlockCoords(newPos).getBlockMetadata(newPos);

                switchFlips.put(id+":"+data, new ArrayList<>());
                switchLoc.put(id+":"+data, pos);
            }
        }
        switchLoc.put("mainStream", ((OffsetPoint) getDungeonRoom().getDungeonRoomInfo().getProperties().get("water-lever")).getBlockPos(getDungeonRoom()));
        switchFlips.put("mainStream", new ArrayList<>());
    }

    private void buildNodes(boolean switchfips) {
        List<OffsetPoint> frontPoints = frontBoard.getOffsetPointList();
        List<OffsetPoint> backPoints = backBoard.getOffsetPointList();

        nodes = new Simulator.Node[25][19];
        waterNodeStart = new Simulator.Pt(9, 0);
        for (int x = 0; x < 19; x++) {
            for (int y = 0; y < 25; y++) {
                OffsetPoint front = frontPoints.get(x *25 +y);
                OffsetPoint back = backPoints.get(x * 25 +y);

                ptMapping.put(new Simulator.Pt(x,y), front.getBlockPos(getDungeonRoom()));
                int frontId = Block.getIdFromBlock(front.getBlock(getDungeonRoom()));
                int backId = Block.getIdFromBlock(back.getBlock(getDungeonRoom()));
                int frontData = front.getData(getDungeonRoom());
                int backData = back.getData(getDungeonRoom());

                if (switchfips) {
                    String switchD;

                    if (switchFlips.containsKey(switchD = (backId + ":" + backData)) || switchFlips.containsKey(switchD = (frontId + ":" + frontData))) {
                        switchFlips.get(switchD).add(new Simulator.Pt(x, y));
                    }
                }

                if (frontId == 0 || frontId == 8  /*flowing*/|| frontId == 9) {
                    if (y == 24) {
                        OffsetPoint pos;
                        if (x != 0) {
                            pos = frontPoints.get((x-1)*25+y);
                        } else {
                            pos = frontPoints.get((x+1) * 25 +y);
                        }

                        int id = Block.getIdFromBlock(pos.getBlock(getDungeonRoom()));
                        int data= pos.getData(getDungeonRoom());
                        waterNodeEnds.put(id+":"+data, new Simulator.Pt(x,y));
                    }

                    nodes[y][x] = new Simulator.Node(frontId != 0 ? frontData >= 8 ? 8 : 8-frontData : 0,
                            frontId == 0 ? Simulator.NodeType.AIR :
                            y == 0 ? Simulator.NodeType.SOURCE :
                                    Simulator.NodeType.WATER, false);
                } else {
                    nodes[y][x] = new Simulator.Node(0, Simulator.NodeType.BLOCK, false);
                }
            }
        }
        if (switchfips) {
            switchFlips.get("mainStream").add(waterNodeStart);
        }
    }
    private void targetDoors() {
        targetDoors.clear();
        for (OffsetPoint offsetPoint : doorsClosed.getOffsetPointList()) {
            if (offsetPoint.getBlock(getDungeonRoom()) != Blocks.air) {
                targetDoors.add(Block.getIdFromBlock(offsetPoint.getBlock(getDungeonRoom()))+":"+offsetPoint.getData(getDungeonRoom()));
            }
        }
    }

    private static final ExecutorService executorService = DungeonsGuide.getDungeonsGuide().registerExecutorService(
            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setThreadFactory(DungeonsGuide.THREAD_FACTORY)
                    .setNameFormat("DG-WaterPuzzle-Calculator").build())
    );

    Simulator.Node[][] lastCopy = null;
    private Future lastCalc;

    private int idx = 0;
    @Override
    public void tick() {
        super.tick();
        if (!FeatureRegistry.SOLVER_WATERPUZZLE.isEnabled()) return;
        if (!argumentsFulfilled) return;
        try {
            buildNodes(false);
            targetDoors();

            Simulator.Node[][] copy = Simulator.clone(nodes);
            boolean changed = !Arrays.deepEquals(lastCopy, copy);
            lastCopy = copy;
            if (!changed) {
                if ((System.currentTimeMillis() - lastUnstable) > 1000)
                    lastStable = System.currentTimeMillis();
            } else {
                lastUnstable = System.currentTimeMillis();
            }
            Simulator.simulateTicks(nodes);
            if ((System.currentTimeMillis() - lastUnstable) > 1000) {
                if (lastCalc == null || lastCalc.isDone()) {
                    lastCalc = executorService.submit(() -> {
                        try {
                            List<Simulator.Pt> targets = targetDoors.stream().map(waterNodeEnds::get).collect(Collectors.toList());
                            List<Simulator.Pt> nonTargets = waterNodeEnds.values().stream().filter(a -> !targets.contains(a)).collect(Collectors.toList());

                            WaterPathfinder waterPathfinder = new WaterPathfinder(copy, targets, nonTargets, switchFlips);
                            WaterPathfinder.NodeNode nodeNode = waterPathfinder.pathfind();
                            LinkedList<WaterPathfinder.NodeNode> solution = new LinkedList<>();
                            if (nodeNode.getParentToMeAction() != null)
                                solution.addFirst(nodeNode);
                            while (nodeNode.getParent() != null) {
                                nodeNode = nodeNode.getParent();
                                if (nodeNode.getParentToMeAction() != null)
                                    solution.addFirst(nodeNode);
                            }
                            this.solutionList = solution;
                            idx = 0;
                            lastStable = System.currentTimeMillis();
                        } catch (Exception e) {
                            lastCopy = null;
                        }
                    });
                }
            }



            if (solutionList != null && solutionList.size() > 0) {
                while (System.currentTimeMillis() - lastStable > (long) (idx) * 2500 + 250) { // water flows 5 ticks/s
                    idx ++;
                }
            }
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        super.drawScreen(partialTicks);
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_WATERPUZZLE.isEnabled()) return;
        if (!argumentsFulfilled) return;
        for (int y = 0; y < nodes.length; y++) {
            for (int x = 0; x < nodes[y].length; x++) {
                Simulator.Node n = nodes[y][x];
                if (n.getNodeType().isWater()) {
                    RenderUtils.highlightBlock(ptMapping.get(new Simulator.Pt(x,y)), new Color(0, 255, 0, 50), partialTicks, true);
                }
            }
        }

        if (solutionList.size() > 0) {
            for (int i = idx; i < solutionList.size(); i++) {

                String key = solutionList.get(i).getParentToMeAction().getKey();
                if (!key.equals("nothing")) {
                    BlockPos pos = switchLoc.get(key);
                    // target:
                    long target = lastStable + 2500L * i;

                    double time = (target-System.currentTimeMillis()) / 1000.0 + 0.051;
                    RenderUtils.drawTextAtWorld(String.format("%.1f", time)+"s", pos.getX()+0.5f, pos.getY()+(i-idx)*0.5f - 0.5f, pos.getZ()+0.5f,
                            time < 0.5 ? 0xFF00FF00 : 0xFFFF5500, 0.05f, false, false, partialTicks);
                }
            }

        }
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorWaterPuzzle> {
        @Override
        public RoomProcessorWaterPuzzle createNew(DungeonRoom dungeonRoom) {
            RoomProcessorWaterPuzzle defaultRoomProcessor = new RoomProcessorWaterPuzzle(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
