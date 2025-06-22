package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonRedstoneKeyState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonRoomDoor2State;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculationRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.PathfindRequest;
import lombok.Data;
import net.minecraft.block.material.MapColor;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class AdditionalInfoCaculatedDungeonRoomInfo {
    private DungeonRoomInfo dungeonRoomInfo;
    private PathfindPreset derivedFrom;

    private RoomPreset roomPreset;

    private String roomShape;
    private String roomType;
    private int roomColor;

    public AdditionalInfoCaculatedDungeonRoomInfo(DungeonRoomInfo dungeonRoomInfo, PathfindPreset pathfindPreset) {
        this.dungeonRoomInfo = dungeonRoomInfo;
        this.derivedFrom = pathfindPreset;

        if (!pathfindPreset.getPresets().containsKey(dungeonRoomInfo.getUuid())) {
            pathfindPreset.getPresets().put(dungeonRoomInfo.getUuid(), new RoomPreset(pathfindPreset, dungeonRoomInfo.getUuid()));
            pathfindPreset.markDirty();
        }
        this.roomPreset = pathfindPreset.getPresets().get(dungeonRoomInfo.getUuid());

        stringifyRoomShapeAndType();

        recalculateAdditionalInfo();
        rematchWithRoomPreset();
    }
    public void stringifyRoomShapeAndType() {

        short shapeShort = dungeonRoomInfo.getShape();
        switch (shapeShort){
            case 0x1:
                roomShape = "1x1";
                break;
            case 0x3:
            case 0x11:
                roomShape = "1x2";
                break;
            case 0x33:
                roomShape = "2x2";
                break;
            case 0x7:
            case 0x111:
                roomShape = "1x3";
                break;
            case 0xF:
            case 0x1111:
                roomShape = "1x4";
                break;
            case 0x13:
            case 0x31:
            case 0x23:
            case 0x32:
                roomShape = "L";
                break;
            default:
                roomShape = shapeShort+"?";
        }


        int j = dungeonRoomInfo.getColor() & 255;

        int color;
        if (j / 4 == 0) {
            color = 0x00000000;
        } else {
            color = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
        }
        this.roomColor = color;
        this.roomType = color+"";
    }

    public void rematchWithRoomPreset() {
        Map<String, List<PathfindPrecalculation>> idsFound = new HashMap<>();

        List<String> notfound = new ArrayList<>();
        List<PathfindPrecalculation> duplicate = new ArrayList<>();
        for (String precalcid : roomPreset.getPrecalculations()) {
            PathfindPrecalculation precalc = PathfindPrecalculationRegistry.getINSTANCE().getById(precalcid);
            if (precalc == null) {
                notfound.add(precalcid);
                continue;
            }
            if (idsFound.containsKey(precalc.getTargetId())) {
                duplicate.add(precalc);
            } else {
                idsFound.put(precalc.getTargetId(), new ArrayList<>());
            }
            idsFound.get(precalc.getTargetId()).add(precalc);
        }

        Map<String, PathfindRequest> required = new HashMap<>();
        for (PathfindRequest request : totalRequiredPrecalculation) {
            required.put(request.getId(), request);
        }


        List<PathfindPrecalculation> unused = new ArrayList<>();
        for (Map.Entry<String, List<PathfindPrecalculation>> entry : idsFound.entrySet()) {
            if (required.containsKey(entry.getKey())) continue;
            unused.addAll(entry.getValue());
        }

        List<PathfindRequest> missing = new ArrayList<>();
        for (Map.Entry<String, PathfindRequest> s : required.entrySet()) {
            if (idsFound.containsKey(s.getKey())) continue;
            missing.add(s.getValue());
        }

        Map<PathfindRequest, List<PathfindPrecalculation>> loaded = new HashMap<>();
        int warnings = 0;
        for (PathfindRequest request : totalRequiredPrecalculation) {
            if (missing.contains(request)) continue;

            loaded.put(request, idsFound.get(request.getId()));
            if (idsFound.get(request.getId()).size() > 1 ||
                    !loaded.get(request).get(0).getAlgorithmSetting().equals(request.getAlgorithmSetting())) {
                warnings++;
            }
        }
        this.missing = missing;
        this.unused = unused;
        this.duplicate = duplicate;
        this.loaded =  loaded;
        this.missingPrecalculation = notfound;
        this.warnings = warnings;
    }

    private List<String> missingPrecalculation;
    private List<PathfindRequest> missing;
    private List<PathfindPrecalculation> unused;
    private List<PathfindPrecalculation> duplicate;
    private Map<PathfindRequest, List<PathfindPrecalculation>> loaded;
    private int warnings;

    public static ActionDAG buildReferencingAllPossibleThings(DungeonRoom dungeonRoom, AlgorithmSetting algorithmSetting) {
        ActionDAGBuilder builder = new ActionDAGBuilder(dungeonRoom);
        for (Map.Entry<String, DungeonMechanicState> value : dungeonRoom.getMechanics().entrySet()) {
            if (value.getValue() instanceof ISecret) {
                try {
                    builder.requires(new ActionChangeState(value.getKey(), "found"), algorithmSetting);
                } catch (PathfindImpossibleException e) {
                    ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to " + value.getKey() + ":found failed due to " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            } else if (value.getValue() instanceof DungeonRedstoneKeyState) {
                try {
                    builder.requires(new ActionChangeState(value.getKey(), "obtained-self"), algorithmSetting);
                } catch (PathfindImpossibleException e) {
                    ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to " + value.getKey() + ":found failed due to " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            } else if (value.getValue() instanceof DungeonRoomDoor2State) {
                try {
                    builder.requires(new ActionChangeState(value.getKey(), "navigate"), algorithmSetting);
                } catch (PathfindImpossibleException e) {
                    ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to door: " + value.getKey() + ":navigate failed due to " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            }
        }
        ActionDAG dag = builder.build();
        return dag;
    }

    private void recalculateAdditionalInfo() {
        DRIWorld driWorld = new DRIWorld(dungeonRoomInfo);
        DungeonContext fakeContext = new DungeonContext("TEST DG", driWorld, roomPreset.getParent());
        DungeonMapLayout dungeonMapLayout = new DungeonMapLayout(
                new Dimension(16, 16),
                5,
                new Point(0, 0),
                new BlockPos(0, 70, 0)
        );
        fakeContext.setScaffoldParser(new DungeonRoomScaffoldParser(dungeonMapLayout, fakeContext));
        DungeonRoom dungeonRoom = new DungeonRoom(fakeContext);

        ActionDAG everything = buildReferencingAllPossibleThings(dungeonRoom, roomPreset.getEffectiveAlgorithmSetting(dungeonRoomInfo));


        Set<String> openMech = new HashSet<>();


        // do dfs...

        Map<ActionChangeState, List<AbstractActionMove>> movementsGenerated = new HashMap<>();

        Stack<ActionDAGNode> currentPath = new Stack<>();
        Stack<ActionChangeState> changeStatesIveseen = new Stack<>();
        currentPath.push(everything.getActionDAGNode());
        boolean[] visited = new boolean[everything.getAllNodes().size()];
        dfs: while (!currentPath.isEmpty()) {
            ActionDAGNode actionDAGNode = currentPath.peek();
            AbstractAction action = actionDAGNode.getAction();

            if (!visited[actionDAGNode.getId()]) {
                List<AbstractActionMove> listOfMoves = changeStatesIveseen.isEmpty() ? null : movementsGenerated.get(changeStatesIveseen.peek());

                if (actionDAGNode.getAction() instanceof ActionChangeState) {
                    if (((ActionChangeState) action).getState().equalsIgnoreCase("open")) {
                        if (!((ActionChangeState) action).getMechanicName().startsWith("superboom") &&
                                !((ActionChangeState) action).getMechanicName().startsWith("crypt") &&
                                !((ActionChangeState) action).getMechanicName().startsWith("prince"))
                            openMech.add(((ActionChangeState) action).getMechanicName());
                    }

                    changeStatesIveseen.push((ActionChangeState) action);
                    if (!movementsGenerated.containsKey((ActionChangeState) action))
                        movementsGenerated.put((ActionChangeState) action, new ArrayList<>());
                } else if (actionDAGNode.getAction() instanceof AtomicAction) {
                    for (AbstractAction actionInAtomicAction : ((AtomicAction) actionDAGNode.getAction()).getActions()) {
                        if (actionInAtomicAction instanceof AbstractActionMove) {
                            if (listOfMoves == null) throw new IllegalStateException("How? - " + everything);
                            listOfMoves.add((AbstractActionMove) actionInAtomicAction);
                        } else if (actionInAtomicAction instanceof ActionChangeState) {
                            if (((ActionChangeState) actionInAtomicAction).getState().equalsIgnoreCase("open")) {
                                if (!((ActionChangeState) actionInAtomicAction).getMechanicName().startsWith("superboom") &&
                                        !((ActionChangeState) actionInAtomicAction).getMechanicName().startsWith("crypt") &&
                                        !((ActionChangeState) actionInAtomicAction).getMechanicName().startsWith("prince"))
                                    openMech.add(((ActionChangeState) actionInAtomicAction).getMechanicName());
                            }
                        }
                    }
                } else if (actionDAGNode.getAction() instanceof AbstractActionMove) {
                    if (listOfMoves == null) throw new IllegalStateException("How? - " + everything);
                    listOfMoves.add((AbstractActionMove) actionDAGNode.getAction());
                }
            }

            visited[actionDAGNode.getId()] = true;

            for (ActionDAGNode allChild : actionDAGNode.getAllChildren()) {
                if (visited[allChild.getId()]) continue;

                currentPath.push(allChild);
                continue dfs;
            }

            currentPath.pop();
            if (!changeStatesIveseen.empty() && changeStatesIveseen.peek() == action) {
                changeStatesIveseen.pop();
            }
        }




        List<String> openMechList = new ArrayList<>(openMech);

        List<RoomStateInfo> stateInfoList = new ArrayList<>();
        List<PathfindRequest> totalRequests = new ArrayList<>();
        for (int i = 0; i < (1 << openMech.size()); i++) {
            Set<String> open = new HashSet<>();
            for (int i1 = 0; i1 < openMechList.size(); i1++) {
                if (((i >> i1) & 0x1) > 0) {
                    open.add(openMechList.get(i1));
                }
            }

            RoomStateInfo stateInfo = new RoomStateInfo(open.stream().sorted(String::compareTo).collect(Collectors.joining(",")));

            for (Map.Entry<ActionChangeState, List<AbstractActionMove>> actionChangeStateListEntry : movementsGenerated.entrySet()) {
                MechanicInfo mechanicInfo = stateInfo.mechanicPrecalculationMap.getOrDefault(actionChangeStateListEntry.getKey().getMechanicName(), new MechanicInfo());

                List<List<OffsetVec3>> toPfTo = new ArrayList<>();
                for (AbstractActionMove action : actionChangeStateListEntry.getValue()) {
                    toPfTo.add(action.getTargetOffsetPointSet());
                }

                for (List<OffsetVec3> offsetVec3s : toPfTo) {
                    PathfindRequest request = new PathfindRequest(roomPreset.getEffectiveAlgorithmSetting(dungeonRoomInfo), dungeonRoomInfo, open, offsetVec3s);
                    request.getId();
                    mechanicInfo.requiredPrecalculationHash.add(request);
                    totalRequests.add(request);
                }

                stateInfo.mechanicPrecalculationMap.put(actionChangeStateListEntry.getKey().getMechanicName(), mechanicInfo);
            }
            stateInfoList.add(stateInfo);
        }

        fakeContext.cleanup();

        this.stateInfos = stateInfoList;
        this.totalRequiredPrecalculation = totalRequests;
    }

    private List<PathfindRequest> totalRequiredPrecalculation = new ArrayList<>();

    private List<RoomStateInfo> stateInfos = new ArrayList<>();

    @Data
    public static class RoomStateInfo {
        private final String stateIdentifier;
        private final Map<String, MechanicInfo> mechanicPrecalculationMap = new HashMap<>();

        public RoomStateInfo(String stateIdentifier) {
            this.stateIdentifier = stateIdentifier;
        }
    }

    @Data
    public static class MechanicInfo {
        private List<PathfindRequest> requiredPrecalculationHash = new ArrayList<>();
    }

}
