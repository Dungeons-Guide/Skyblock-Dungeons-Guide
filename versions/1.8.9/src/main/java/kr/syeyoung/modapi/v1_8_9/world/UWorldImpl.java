package kr.syeyoung.modapi.v1_8_9.world;

import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityImpl;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityPlayerImpl;
import kr.syeyoung.modapi.v1_8_9.item.UItemStackImpl;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UMapData;
import kr.syeyoung.modapi.world.UWorld;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UWorldImpl implements UWorld {
    private final World delegate;
    private BlockStateRegistryImpl stateRegistry;

    public UWorldImpl(World delegate) {
        this.delegate = delegate;
    }

    public UEntity getEntityById(int id) {
        Entity e = delegate.getEntityByID(id);
        return e == null ? null : UEntityDelegateFactory.createEntityFor(e);
    }

    public List<UEntity> getLoadedUEntityList() {
        List<UEntity> converted = new ArrayList<>();
        for (Entity entity : delegate.getLoadedEntityList()) {
            converted.add(UEntityDelegateFactory.createEntityFor(entity));
        }
        return converted;
    }

    public UEntityPlayer getPlayerEntityByUuid(UUID uuid) {
        EntityPlayer player = delegate.getPlayerEntityByUUID(uuid);
        return player == null ? null : new UEntityPlayerImpl(player);
    }


    public List<UEntity> getEntitiesWithinAabb(EntityType entityType, AABB bb) {
        List<Entity> entities = delegate.getEntitiesWithinAABB(
                UEntityPlayerImpl.bimap.inverse().get(entityType),
                new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ)
        );

        List<UEntity> mapped = new ArrayList<>();
        for (Entity e : entities) {
            mapped.add(UEntityDelegateFactory.createEntityFor(e));
        }
        return mapped;
    }

    @Override
    public UMapData getMapData(UItemStack itemMap) {
        ItemStack itemStack = ((UItemStackImpl) itemMap).getDelegate();
        MapData mapData = Items.filled_map.getMapData(itemStack, delegate);
        if (mapData == null) return null;
        return new UMapDataImpl(mapData);
    }

    public UEntityPlayer getUPlayerEntityByName(String name) {
        EntityPlayer entityPlayer = delegate.getPlayerEntityByName(name);
        return entityPlayer == null ? null : new UEntityPlayerImpl(entityPlayer);
    }

    public List<UEntity> getEntities(EntityType entityType) {
        List<Entity> entities = delegate.getEntities(UEntityImpl.bimap.inverse().get(entityType), a -> true);

        List<UEntity> mapped = new ArrayList<>();
        for (Entity e : entities) {
            mapped.add(UEntityDelegateFactory.createEntityFor(e));
        }
        return mapped;
    }


    private ThreadLocal<BlockPos.MutableBlockPos> posThreadLocal = ThreadLocal.withInitial(() -> new BlockPos.MutableBlockPos());
    @Override
    public UBlockState getBlockStateAt(int x, int y, int z) {
        BlockPos.MutableBlockPos pos = posThreadLocal.get();
        pos.set(x,y,z);
        IBlockState blockState = delegate.getBlockState(pos);
        int stateId = Block.getStateId(blockState);
        return stateRegistry.getByStateId(stateId);
    }

    @Override
    public UBlockState getBlockStateAt(VectorI3D blockPos) {
        BlockPos.MutableBlockPos pos = posThreadLocal.get();
        pos.set(blockPos.x, blockPos.y, blockPos.z);
        IBlockState blockState = delegate.getBlockState(pos);
        int stateId = Block.getStateId(blockState);
        return stateRegistry.getByStateId(stateId);
    }
}
