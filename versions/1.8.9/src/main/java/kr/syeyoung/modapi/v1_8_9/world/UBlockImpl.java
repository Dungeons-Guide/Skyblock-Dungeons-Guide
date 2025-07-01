package kr.syeyoung.modapi.v1_8_9.world;

import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.util.RaycastResult;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.UBlock;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class UBlockImpl implements UBlock {
    @Getter
    private Block delegate;
    private String id;

    public UBlockImpl(Block block) {
        this.delegate = block;
        this.id = block.getRegistryName();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLocalizedName() {
        return delegate.getLocalizedName();
    }

    private static final ThreadLocal<FakeWorld> fakeWorldGen = ThreadLocal.withInitial(() -> new FakeWorld());
    private static final ThreadLocal<BlockPos.MutableBlockPos> posGen = ThreadLocal.withInitial(() -> new BlockPos.MutableBlockPos(0, 0, 0));

    @Override
    public RaycastResult collisionRaytrace(IBlockAccessible worldIn, VectorI3D pos, Vector3D start, Vector3D end) {
        FakeWorld fakeWorld = fakeWorldGen.get();
        fakeWorld.setAccessible(worldIn);
        BlockPos.MutableBlockPos pos2 = posGen.get();
        pos2.set(pos.x, pos.y, pos.z);
        MovingObjectPosition position = delegate.collisionRayTrace(fakeWorld, pos2, new Vec3(start.x, start.y, start.z), new Vec3(end.x, end.y, end.z));

        if (position == null) return new RaycastResult(null, RaycastResult.HitType.MISS, null, null);
        return new RaycastResult(
                position.getBlockPos() == null ? null : new VectorI3D(position.getBlockPos().getX(), position.getBlockPos().getY(), position.getBlockPos().getZ()),
                position.typeOfHit == MovingObjectPosition.MovingObjectType.MISS ? RaycastResult.HitType.MISS :
                        position.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY ? RaycastResult.HitType.ENTITY :
                                RaycastResult.HitType.BLOCK,
                position.hitVec == null ? null : new Vector3D(position.hitVec.xCoord, position.hitVec.yCoord, position.hitVec.zCoord),
                position.entityHit == null ? null : UEntityDelegateFactory.createEntityFor(position.entityHit)
        );
    }

    @Override
    public float getBlockHardness(IBlockAccessible worldIn, VectorI3D pos) {
        FakeWorld fakeWorld = fakeWorldGen.get();
        fakeWorld.setAccessible(worldIn);
        BlockPos.MutableBlockPos pos2 = posGen.get();
        pos2.set(pos.x, pos.y, pos.z);
        return delegate.getBlockHardness(fakeWorld, pos2);
    }

    @Override
    public boolean isHarvestPickaxe() {
        Material material = delegate.getMaterial();
        return material == Material.rock || material  == Material.anvil || material  == Material.iron;
    }

    @Override
    public boolean isHarvestAxe() {
        Material material = delegate.getMaterial();
        return material == Material.wood || material == Material.plants || material == Material.vine;
    }

    @Override
    public boolean isHarvestShovel() {
        return false;
    }
}
