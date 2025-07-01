package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonBreakableWallState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonTombState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.CoordinateMapBackedPathfindWorld;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.world.*;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class DRIBackedBlockMap implements ICoordinateMap<UBlockState>, UWorld, IBlockAccessible {
    @Getter
    private DungeonRoomInfo dungeonRoomInfo;
    private List<String> openMechanics;
    private int shape;

    private HashSet<VectorI3D> poses = new HashSet<>();
    private HashSet<VectorI3D> open = new HashSet<>();
    private AlgorithmSetting algorithmSetting;

    @Getter
    private CoordinateMapBackedPathfindWorld pathfindWorld;

    private RoomBounds roomBounds;

    public DRIBackedBlockMap(DungeonRoomInfo dungeonRoomInfo) {
        this(dungeonRoomInfo, Collections.emptyList());
    }

    public DRIBackedBlockMap(DungeonRoomInfo dungeonRoomInfo, List<String> openMechanics) {
        this.dungeonRoomInfo = dungeonRoomInfo;
        this.openMechanics = openMechanics;
        this.shape = dungeonRoomInfo.getShape();

        for (DungeonMechanicData value : dungeonRoomInfo.getMechanics().values()) {
            if (value instanceof DungeonTombState.DungeonTombData) {
                for (OffsetPoint offsetPoint : ((DungeonTombState.DungeonTombData) value).blockedPoints()) {
                    poses.add(new VectorI3D(offsetPoint.getX(), offsetPoint.getY() + 70, offsetPoint.getZ()));
                }
            } else if (value instanceof DungeonBreakableWallState.DungeonBreakableWallData) {
                for (OffsetPoint offsetPoint : ((DungeonBreakableWallState.DungeonBreakableWallData) value).blockedPoints()) {
                    poses.add(new VectorI3D(offsetPoint.getX(), offsetPoint.getY() + 70, offsetPoint.getZ()));
                }
            }
        } // TODO: construct actual mechanics.

        for (String openMechanic : openMechanics) {
            WorldMutatingMechanicData routeBlocker = (WorldMutatingMechanicData) dungeonRoomInfo.getMechanics().get(openMechanic);
            for (OffsetPoint offsetPoint : routeBlocker.blockedPoints()) {
                open.add(new VectorI3D(offsetPoint.getX(), offsetPoint.getY() +70, offsetPoint.getZ()));
            }
        }


        PathfindPreset preset = FeatureRegistry.SECRET_PRECALC_LIST.getSelectedPreset();
        AlgorithmSetting algorithmSetting1 = preset.getRoomPreset(dungeonRoomInfo.getUuid()).getEffectiveAlgorithmSetting(dungeonRoomInfo);
        this.algorithmSetting = algorithmSetting1;

        pathfindWorld = new CoordinateMapBackedPathfindWorld(this, algorithmSetting, roomBounds = new RoomBounds(
                dungeonRoomInfo.getShape(),
                new VectorI3D(0, 70, 0),
                new VectorI3D(dungeonRoomInfo.getWidth() -1, 70, dungeonRoomInfo.getLength() - 1)
        ), poses);
    }


    private UBlockState air = ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.AIR);
    private VectorI3D vectorI3D = new VectorI3D(0,0,0);
    @Override
    public UBlockState getBlock(int x, int y, int z) {
        if (!roomBounds.canAccessAbsolute(x,y,z)) {
            return air;
        }
        vectorI3D.x = x; vectorI3D.y=  y; vectorI3D.z = z;
        if (open.contains(vectorI3D)) {
            return air;
        }
        return dungeonRoomInfo.getBlock(x,y-70,z, 0);
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return roomBounds.canAccessAbsolute(x,y,z);
    }

    @Override
    public int getMinX() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getMinZ() {
        return 0;
    }

    @Override
    public int getMaxX() {
        return dungeonRoomInfo.getWidth();
    }

    @Override
    public int getMaxY() {
        return 256;
    }

    @Override
    public int getMaxZ() {
        return dungeonRoomInfo.getLength();
    }

    @Override
    public int getLenX() {
        return ICoordinateMap.super.getLenX();
    }

    @Override
    public int getLenY() {
        return ICoordinateMap.super.getLenY();
    }

    @Override
    public int getLenZ() {
        return ICoordinateMap.super.getLenZ();
    }



    @Override
    public UBlockState getBlockStateAt(int x, int y, int z) {
        return getBlock(x,y,z);
    }

    @Override
    public UBlockState getBlockStateAt(VectorI3D blockPos) {
        return getBlockStateAt(blockPos.x, blockPos.y, blockPos.z);
    }

    @Override
    public UTileEntity getTileEntityAt(int x, int y, int z) {
        return null;
    }

    @Override
    public UTileEntity getTileEntityAt(VectorI3D blockPos) {
        return null;
    }

    @Override
    public UEntity getEntityById(int id) {
        return null;
    }

    @Override
    public List<UEntity> getLoadedUEntityList() {
        return Collections.emptyList();
    }

    @Override
    public UEntityPlayer getPlayerEntityByUuid(UUID uuid) {
        return null;
    }

    @Override
    public List<UEntity> getEntitiesWithinAabb(EntityType type, AABB bb) {
        return Collections.emptyList();
    }

    @Override
    public UMapData getMapData(UItemStack itemMap) {
        return null;
    }

    @Override
    public UEntityPlayer getUPlayerEntityByName(String name) {
        return null;
    }

    @Override
    public List<UEntity> getEntities(EntityType type) {
        return Collections.emptyList();
    }

    @Override
    public UChunk getChunkAt(int x, int z) {
        return null;
    }

    @Override
    public Object getWorld() {
        return null;
    }
}
