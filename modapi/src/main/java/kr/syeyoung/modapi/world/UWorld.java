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

    List<UEntity> getLoadedUEntityList();

    UEntityPlayer getPlayerEntityByUuid(UUID uuid);

    List<UEntity> getEntitiesWithinAabb(EntityType type, AABB bb);

    UMapData getMapData(UItemStack itemMap);

    UEntityPlayer getUPlayerEntityByName(String name);

    List<UEntity> getEntities(EntityType type);
}
