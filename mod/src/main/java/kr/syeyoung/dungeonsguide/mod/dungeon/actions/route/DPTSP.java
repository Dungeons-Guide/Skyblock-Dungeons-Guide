package kr.syeyoung.dungeonsguide.mod.dungeon.actions.route;

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonDoorState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonOnewayDoorState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.TSPCache;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.locks.LockSupport;

import java.util.*;
import java.util.stream.Collectors;

public class DPTSP {
    private final RoomPresetPathPlanner pathPlanner;
    private final ActionDAG dag;
    private final Vec3 start;
    private final double startX;
    private final double startY;
    private final double startZ;
    private final DungeonRoom dungeonRoom;
    private TSPCache cache;


    private ActionDAGNode[] bitNodes;
    private int[] requireIdBitMapping;

    private ActionDAGNode[][] orNodes;
    private int[] orIdIdxMapping;
    private boolean[] sanity;

    private int[] nodeType;
    private ActionDAGNode[] everyNode;

    private List<String> mechanicNames;

    private long[] require;
    private int[][] or;

    int requireBitSize;
    int stBitset;

    int[] solution;

    public DPTSP(ActionDAG dag, Vec3 start, DungeonRoom dungeonRoom) {
        this.pathPlanner = new RoomPresetPathPlanner(dungeonRoom.getContext().getPreset().getRoomPreset(dungeonRoom.getDungeonRoomInfo().getUuid()));
        this.dag = dag;
        this.start = start;
        this.startX = start.xCoord;
        this.startY = start.yCoord;
        this.startZ = start.zCoord;
        this.dungeonRoom = dungeonRoom;

        setup();
        solve();
    }



    private RoomState roomState;

    private static boolean nativeLoaded = true;
//
//    // jni requires.
//    public EvalRes evaluate(double x, double y, double z, int mechanic, int node) {
//        roomState.setPlayerPos(new Vec3(x, y, z));
//        roomState.setOpenMechanicsBitset(mechanic);
//        double cost = everyNode[node].getAction().evalulateCost(roomState, dungeonRoom, cache, pathPlanner);
//        return new EvalRes(roomState.getPlayerPos().xCoord, roomState.getPlayerPos().yCoord, roomState.getPlayerPos().zCoord, roomState.openMechanicsBitset, cost);
//    }
//
//    @AllArgsConstructor
//    public static class EvalRes {
//        public double x;
//        public double y;
//        public double z;
//        public int mechanic;
//        public double cost;
//    }

    public void solve() {

        if (nativeLoaded) {
            try {
                TimeCache.ensureStarted();
                final long deadline = TimeCache.now + 10000;
                long handle = startCoroutine();
                try {
                    while (true) {
                        if (TimeCache.now > deadline) {
                            ChatTransmitter.addToQueue("§fSolver took too long (10s) YIKES!!!");
                            ChatTransmitter.addToQueue("Room: " + dungeonRoom.getDungeonRoomInfo().getName());
                            ChatTransmitter.addToQueue("Roomsate is :" + roomState);
                            break;
                        }
                        roomState.setPlayerPos(new Vec3(getX(handle), getY(handle), getZ(handle)));
                        roomState.setOpenMechanicsBitset(getMech(handle));
                        double cost = everyNode[getNode(handle)].getAction().evalulateCost(roomState, dungeonRoom, cache, pathPlanner);
                        boolean res = resumeCoroutine(handle, roomState.getPlayerPos().xCoord, roomState.getPlayerPos().yCoord, roomState.getPlayerPos().zCoord, roomState.openMechanicsBitset, cost);
                        if (!res) break;
                    }
                    solution = getResult(handle, dag.getActionDAGNode().getId());
                } finally {
                    destoryCoroutine(handle);
                }
            } catch (UnsatisfiedLinkError e) {
                ChatTransmitter.addToQueue("§eDungeons Guide :: §fTSP Path Planner :: §cNative Library is not loaded. Falling back to old planners");
                nativeLoaded = true;
                throw e;
            }
        } else {
            ChatTransmitter.addToQueue("§eDungeons Guide :: §fTSP Path Planner :: §cNative Library is not loaded. Falling back to old planners");
            throw new RuntimeException("");
        }
    }
/*
* 6dcddc04-c094-4b9b-8c05-b101e8f3dd27::18.0,20.0,24.5;18.0,20.0,25.0;18.5,20.0,23.5;18.5,20.0,24.0;18.5,20.0,24.5;18.5,20.0,25.0;19.0,20.0,23.5;19.0,20.0,24.0;19.0,20.0,24.5;19.0,21.0,25.0;19.5,20.0,23.5;19.5,20.0,24.0;19.5,20.0,24.5;19.5,21.0,25.0;19.5,21.0,25.5
* 6dcddc04-c094-4b9b-8c05-b101e8f3dd27::18.0,20.0,26.0;18.0,20.0,26.5;18.5,20.0,25.5;18.5,20.0,26.0;18.5,20.0,26.5;18.5,20.0,27.0;18.5,20.0,27.5;18.5,20.0,28.0;19.0,21.0,25.5;19.0,21.0,26.0;19.0,20.0,26.5;19.0,20.0,27.0;19.0,20.0,27.5;19.5,21.0,26.0;19.5,20.0,26.5;19.5,20.0,27.0;19.5,20.0,27.5
*
* 6dcddc04-c094-4b9b-8c05-b101e8f3dd27::18.0,20.0,24.5;18.0,20.0,25.0;18.5,20.0,23.5;18.5,20.0,24.0;18.5,20.0,24.5;18.5,20.0,25.0;18.5,20.0,25.5;19.0,20.0,23.5;19.0,20.0,24.0;19.0,20.0,24.5;19.0,21.0,25.0;19.0,21.0,25.5;19.5,20.0,23.5;19.5,20.0,24.0;19.5,20.0,24.5;19.5,21.0,25.0
* 6dcddc04-c094-4b9b-8c05-b101e8f3dd27::18.0,20.0,26.0;18.0,20.0,26.5;18.5,20.0,26.0;18.5,20.0,26.5;18.5,20.0,27.0;18.5,20.0,27.5;18.5,20.0,28.0;19.0,21.0,26.0;19.0,20.0,26.5;19.0,20.0,27.0;19.0,20.0,27.5;19.5,21.0,25.5;19.5,21.0,26.0;19.5,20.0,26.5;19.5,20.0,27.0;19.5,20.0,27.5
*
* */
    private void setup() {
        List<ActionDAGNode> dagNodeList = new ArrayList<>();
        int[] nodeStatus = dag.getNodeStatusAll();

        List<ActionDAGNode> allNodes = dag.getAllNodes();

        int nodeCount = allNodes.size();
        requireIdBitMapping = new int[nodeCount];
        orIdIdxMapping = new int[nodeCount];
        nodeType = new int[nodeCount];
        require = new long[nodeCount];
        or = new int[nodeCount][];
        sanity = new boolean[nodeCount];

        

        label: for (int i = 0; i < allNodes.size(); i++) {
            ActionDAGNode node = allNodes.get(i);
            for (ActionDAGNode actionDAGNode : node.getRequiredBy()) {
                if (actionDAGNode.getOr().isEmpty()) continue;
                continue label;
            } // ignore optional.

            nodeType[node.getId()] = 1;
            requireIdBitMapping[node.getId()] = dagNodeList.size();
            dagNodeList.add(node);
        }

        bitNodes = dagNodeList.toArray(new ActionDAGNode[0]);
        requireBitSize = bitNodes.length;

        long mult = 1;
        List<ActionDAGNode[]> orNodes = new ArrayList<>();
        for (ActionDAGNode allNode : allNodes) {
            if (allNode.getOr().isEmpty()) continue;
            ActionDAGNode[] ornode = new ActionDAGNode[allNode.getOr().size()+1];
            for (int i = 0; i < allNode.getOr().size(); i++) {
                orIdIdxMapping[allNode.getOr().get(i).getId()] = i+1;
                ornode[i+1] = allNode.getOr().get(i);
                nodeType[allNode.getOr().get(i).getId()] = orNodes.size() + 2;
            }
            orNodes.add(ornode);
            mult *= ornode.length;
        }
        this.orNodes = orNodes.toArray(new ActionDAGNode[0][]);



        mechanicNames = dungeonRoom.getMechanics().entrySet().stream().filter(a -> a.getValue() instanceof DungeonDoorState || a.getValue() instanceof DungeonOnewayDoorState)
                .map(a -> a.getKey()).collect(Collectors.toList());

        stBitset = 0;
        for (int i = 0; i < mechanicNames.size(); i++) {
            String mechanicName = mechanicNames.get(i);
            if (!((WorldMutatingMechanicState)dungeonRoom.getMechanics().get(mechanicName)).isBlocking(dungeonRoom)) {
                stBitset |= 1 << i;
            }
        }

        for (int i = 0; i < nodeStatus.length; i++) {
            if (nodeStatus[i] == 1 || nodeStatus[i] == 2)
                nodeType[i] = 0;
        }

        everyNode = allNodes.toArray(new ActionDAGNode[0]);
        for (int i = 0; i < everyNode.length; i++) {
            require[i] = 0;
            for (int j = 0; j < everyNode[i].getRequire().size(); j++) {
                if (nodeType[everyNode[i].getRequire().get(j).getId()] != 1) continue;
                require[i] |= (1L << requireIdBitMapping[everyNode[i].getRequire().get(j).getId()]);
            }
            or[i] = new int[everyNode[i].getOr().size()];
            for (int j = 0; j < everyNode[i].getOr().size(); j++) {
                or[i][j] = everyNode[i].getOr().get(j).getId();
            }
            sanity[i] = everyNode[i].getAction().isSanityCheck();
        }

        System.out.println(requireBitSize + " / " + mult);

        roomState = new RoomState(mechanicNames);
        roomState.setDungeonRoom(dungeonRoom);
        cache = new TSPCache((GeneralRoomProcessor) dungeonRoom.getRoomProcessor(), dungeonRoom, Collections.EMPTY_LIST, Collections.singletonList(start));
    }

    private native long startCoroutine();
    private native boolean resumeCoroutine(long handle, double x, double y, double z, int mech, double cost);
    private native double getX(long handle);
    private native double getY(long handle);
    private native double getZ(long handle);
    private native int getNode(long handle);
    private native int getMech(long handle);
    private native int[] getResult(long handle, int target);
    private native void destoryCoroutine(long handle);



    public List<ActionDAGNode> reconstructPath() {
        List<ActionDAGNode> nodes = new ArrayList<>();
        for (int i : solution) {
            nodes.add(everyNode[i]);
        }
        return nodes;
    }




}

class TimeCache {
    static volatile long now;
    private static volatile boolean started = false;

    static {
        start();
    }

    static void ensureStarted() {
        // no-op, forces class initialization
    }

    private static synchronized void start() {
        if (started) return;
        started = true;

        now = System.currentTimeMillis();
        Thread t = new Thread(() -> {
            while (true) {
                now = System.currentTimeMillis();
                LockSupport.parkNanos(50_000_000);
            }
        }, "TimeCache");
        t.setDaemon(true);
        t.start();
    }

    private TimeCache() {}
}