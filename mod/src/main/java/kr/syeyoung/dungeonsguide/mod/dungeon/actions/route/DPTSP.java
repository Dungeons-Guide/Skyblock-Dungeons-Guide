package kr.syeyoung.dungeonsguide.mod.dungeon.actions.route;

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

import java.util.*;
import java.util.stream.Collectors;

public class DPTSP {
    private final RoomPresetPathPlanner pathPlanner;
    private final ActionDAG dag;
    private final Vec3 start;
    private final DungeonRoom dungeonRoom;



    private ActionDAGNode[] bitNodes;
    private int[] requireIdBitMapping;

    private ActionDAGNode[][] orNodes;
    private int[] orIdIdxMapping;

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
        this.dungeonRoom = dungeonRoom;

        setup();
    }

    public void solve() {
        solution = runTSPNative();
    }

    private void setup() {
        List<ActionDAGNode> dagNodeList = new ArrayList<>();
        int[] nodeStatus = dag.getNodeStatus(0);


        requireIdBitMapping = new int[dag.getAllNodes().size()];
        orIdIdxMapping = new int[dag.getAllNodes().size()];
        nodeType = new int[dag.getAllNodes().size()];
        require = new long[dag.getAllNodes().size()];
        or = new int[dag.getAllNodes().size()][];


        label: for (int i = 0; i < dag.getAllNodes().size(); i++) {
            ActionDAGNode node = dag.getAllNodes().get(i);
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
        for (ActionDAGNode allNode : dag.getAllNodes()) {
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

        int bitset = 0;
        for (int i = 0; i < mechanicNames.size(); i++) {
            String mechanicName = mechanicNames.get(i);
            if (!((WorldMutatingMechanicState)dungeonRoom.getMechanics().get(mechanicName)).isBlocking(dungeonRoom)) {
                bitset |= 1 << i;
            }
        }
        stBitset = bitset;

        for (int i = 0; i < nodeStatus.length; i++) {
            if (nodeStatus[i] == 1 || nodeStatus[i] == 2)
                nodeType[i] = 0;
        }

        everyNode = dag.getAllNodes().toArray(new ActionDAGNode[0]);
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
        }

        System.out.println(requireBitSize + " / " + mult);
    }


    @AllArgsConstructor @Data @EqualsAndHashCode
    public class VisitedSet implements Comparable<VisitedSet> {
        private long requiresBitset;
        private int[] orChoice;

        public VisitedSet clone() {
            return new VisitedSet(requiresBitset, Arrays.copyOf(orChoice, orChoice.length));
        }

        public boolean contains(int node) {
            if (nodeType[node] == 0) return false;
            if (nodeType[node] == 1) return (requiresBitset & (1L << requireIdBitMapping[node])) > 0;
            int orIdx = nodeType[node]-2;
            int indexInIndx = orIdIdxMapping[node];
            return orChoice[orIdx] == indexInIndx;
        }
        public boolean canBeAdded(int node) {
            if (nodeType[node] == 0) return false;
            if (nodeType[node] == 1) return (requiresBitset & (1L << requireIdBitMapping[node])) == 0;
            int orIdx = nodeType[node]-2;
            return orChoice[orIdx] == 0;
        }

        public void add(int node) {
            if (nodeType[node] == 0) return;
            if (nodeType[node] == 1) {
                requiresBitset |= (1L << requireIdBitMapping[node]);
                return;
            }
            int orIdx = nodeType[node]-2;
            int indexInIndx = orIdIdxMapping[node];
            orChoice[orIdx] = indexInIndx;
        }
        public VisitedSet subtract(int node) {
            if (nodeType[node] == 0) return null;
            if (nodeType[node] == 1) return new VisitedSet(requiresBitset ^ (1L << requireIdBitMapping[node]), Arrays.copyOf(orChoice, orChoice.length));
            int orIdx = nodeType[node]-2;
            int indexInIndx = orIdIdxMapping[node];
            int[] newOrChoice = Arrays.copyOf(orChoice, orChoice.length);
            newOrChoice[orIdx] = 0;
            return new VisitedSet(requiresBitset, newOrChoice);
        }


        @Override
        public int compareTo(@NotNull DPTSP.VisitedSet o) {
            if (this.requiresBitset != o.requiresBitset) return this.requiresBitset < o.requiresBitset ? 1 : -1;
            for (int i = 0; i < this.orChoice.length; i++) {
                if (this.orChoice[i] != o.orChoice[i]) return this.orChoice[i] < o.orChoice[i] ? 1 : -1;
            }
            return 0;
        }
    }

    @AllArgsConstructor @Data @EqualsAndHashCode
    public static class MemoKey implements Comparable<MemoKey> {
        private VisitedSet set;
        private int last;


        @Override
        public int compareTo(@NotNull DPTSP.MemoKey o) {
            int cmp = set.compareTo(o.set);
            if (cmp != 0) return cmp;
            if (this.last != o.last) return this.last < o.last ? 1 : -1;
            return 0;
        }
    }

    @AllArgsConstructor @Data
    public static class MemoElement {
        private double cost;
        private Vec3 pos;
        private MemoKey prev;
        private int openKey;
    }

    private boolean canVisit(MemoKey memoKey, int node) {
        if (nodeType[node] == 0) return false; // don't visit.
        if (!memoKey.set.canBeAdded(node)) return false;
        if ((memoKey.set.requiresBitset & require[node]) != require[node]) return false;

        boolean flag = or[node].length != 0;
        for (int nodeID : or[node]) {
            if (nodeType[nodeID] == 0 || memoKey.set.contains(nodeID)) {
                flag = false;
                break;
            }
        }
        if (flag) return false;
        return true;
    }

    private native int[] runTSPNative();


    public List<ActionDAGNode> reconstructPath() {
        List<ActionDAGNode> nodes = new ArrayList<>();
        for (int i : solution) {
            nodes.add(everyNode[i]);
        }
        return nodes;
    }
}
