package kr.syeyoung.modapi.world;

import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.item.UItemStack;

import java.util.List;
import java.util.UUID;

public interface UWorld extends IBlockAccessible {
    UEntity getEntityById(int id);

    UEntityPlayer getPlayerEntityByUuid(UUID uuid);

    List<UEntity> getEntitiesWithinAabb(EntityType type, AABB bb);
    List<UEntity> getEntitiesWithinAabb(AABB bb);

    UMapData getMapData(UItemStack itemMap);

    UEntityPlayer getUPlayerEntityByName(String name);

    UChunk getChunkAt(int x, int z);

    Object getWorld();

    List<UEntity> getEntities();
}
