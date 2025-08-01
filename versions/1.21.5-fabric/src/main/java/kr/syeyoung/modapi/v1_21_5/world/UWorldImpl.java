package kr.syeyoung.modapi.v1_21_5.world;

import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_21_5.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_21_5.entity.UEntityPlayerImpl;
import kr.syeyoung.modapi.v1_21_5.item.UItemStackImpl;
import kr.syeyoung.modapi.v1_21_5.world.entities.UTileEntityChestImpl;
import kr.syeyoung.modapi.v1_21_5.world.entities.UTileEntityImpl;
import kr.syeyoung.modapi.v1_21_5.world.entities.UTileEntitySkullImpl;
import kr.syeyoung.modapi.world.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UWorldImpl implements UWorld {
    private final World delegate;
    private BlockStateRegistryImpl stateRegistry;

    public UWorldImpl(World delegate, BlockStateRegistryImpl stateRegistry) {
        this.delegate = delegate;
        this.stateRegistry =stateRegistry;
    }

    public UEntity getEntityById(int id) {
        Entity e = delegate.getEntityById(id);
        return e == null ? null : UEntityDelegateFactory.createEntityFor(e);
    }

    public UEntityPlayer getPlayerEntityByUuid(UUID uuid) {
        PlayerEntity player = delegate.getPlayerByUuid(uuid);
        return player == null ? null : new UEntityPlayerImpl(player);
    }


    public List<UEntity> getEntitiesWithinAabb(EntityType entityType, AABB bb) {
        List<? extends Entity> entities = delegate.getEntitiesByType(
                TypeFilter.instanceOf(UEntityPlayerImpl.bimap.inverse().get(entityType)),
                new Box(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ),
                (e) -> true
        );

        List<UEntity> mapped = new ArrayList<>();
        for (Entity e : entities) {
            mapped.add(UEntityDelegateFactory.createEntityFor(e));
        }
        return mapped;
    }

    @Override
    public List<UEntity> getEntitiesWithinAabb(AABB bb) {
        List<? extends Entity> entities = delegate.getOtherEntities(
                null,
                new Box(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ)
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
        MapState mapData = FilledMapItem.getMapState(itemStack, delegate);
        if (mapData == null) return null;
        return new UMapDataImpl(mapData);
    }

    public UEntityPlayer getUPlayerEntityByName(String name) {
        for (PlayerEntity player : delegate.getPlayers()) {
            if (Objects.equals(player.getName().getString(), name)) return new UEntityPlayerImpl(player); // TODO: ???
        }
        return null;
    }

    private ThreadLocal<BlockPos.Mutable> posThreadLocal = ThreadLocal.withInitial(() -> new BlockPos.Mutable());
    @Override
    public UBlockState getBlockStateAt(int x, int y, int z) {
        BlockPos.Mutable pos = posThreadLocal.get();
        pos.set(x,y,z);
        BlockState blockState = delegate.getBlockState(pos);
        int stateId = Block.STATE_IDS.getRawId(blockState);
        return stateRegistry.getByStateId(stateId);
    }

    @Override
    public UBlockState getBlockStateAt(VectorI3D blockPos) {
        BlockPos.Mutable pos = posThreadLocal.get();
        pos.set(blockPos.x, blockPos.y, blockPos.z);
        BlockState blockState = delegate.getBlockState(pos);
        int stateId = Block.STATE_IDS.getRawId(blockState);
        return stateRegistry.getByStateId(stateId);
    }

    @Override
    public UTileEntity getTileEntityAt(int x, int y, int z) {
        BlockPos.Mutable pos = posThreadLocal.get();
        pos.set(x, y, z);
        BlockEntity tileEntity = delegate.getBlockEntity(pos);
        if (tileEntity instanceof ChestBlockEntity)
            return new UTileEntityChestImpl((ChestBlockEntity) tileEntity);
        else if (tileEntity instanceof SkullBlockEntity)
            return new UTileEntitySkullImpl((SkullBlockEntity) tileEntity);
        else if (tileEntity != null)
            return new UTileEntityImpl(tileEntity);

        return null;
    }

    @Override
    public UTileEntity getTileEntityAt(VectorI3D blockPos) {
        return getTileEntityAt(blockPos.x, blockPos.y, blockPos.z);
    }

    @Override
    public UChunk getChunkAt(int x, int z) {
        Chunk c = delegate.getChunk(x, z);
        return new UChunkImpl(c, stateRegistry);
    }

    @Override
    public Object getWorld() {
        return delegate;
    }
}
