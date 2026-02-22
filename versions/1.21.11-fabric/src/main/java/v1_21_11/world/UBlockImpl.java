package v1_21_11.world;

import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.util.RaycastResult;
import kr.syeyoung.modapi.v1_21_5.world.FakeWorld;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.UBlock;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class UBlockImpl implements UBlock {
    @Getter
    private Block delegate;
    private String id;

    public UBlockImpl(Block block) {
        this.delegate = block;
        this.id = Registries.BLOCK.getId(block).toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLocalizedName() {
        return delegate.getName().getString();
    }

    private static final ThreadLocal<FakeWorld> fakeWorldGen = ThreadLocal.withInitial(() -> new FakeWorld());
    private static final ThreadLocal<BlockPos.Mutable> posGen = ThreadLocal.withInitial(() -> new BlockPos.Mutable(0, 0, 0));

    @Override
    public RaycastResult collisionRaytrace(IBlockAccessible worldIn, VectorI3D pos, Vector3D start, Vector3D end) {
        FakeWorld fakeWorld = fakeWorldGen.get();
        fakeWorld.setAccessible(worldIn);
        BlockPos.Mutable pos2 = posGen.get();
        pos2.set(pos.x, pos.y, pos.z);
        Vec3d start3 =  new Vec3d(start.x, start.y, start.z);
        Vec3d end3 = new Vec3d(end.x, end.y, end.z);

        BlockState blockState = ((UBlockStateImpl) worldIn.getBlockStateAt(pos)).getDelegate();
        VoxelShape shape = blockState.getOutlineShape(fakeWorld, pos2);

        BlockHitResult blockHitResult2;
        BlockHitResult blockHitResult = shape.raycast(
               start3, end3, pos2);

        if (blockHitResult != null &&
                (blockHitResult2 = blockState.getRaycastShape(fakeWorld, pos2).raycast(start3, end3, pos2)) != null &&
                blockHitResult2.getPos().subtract(start3).lengthSquared() < blockHitResult.getPos().subtract(start3).lengthSquared()) {
            blockHitResult =  blockHitResult.withSide(blockHitResult2.getSide());
        }

        if (blockHitResult == null || blockHitResult.getType() == HitResult.Type.MISS) return new RaycastResult(null, RaycastResult.HitType.MISS, null, null);
        return new RaycastResult(
                pos,
                RaycastResult.HitType.BLOCK,
                null, null);
    }

    @Override
    public float getBlockHardness(IBlockAccessible worldIn, VectorI3D pos) {
        return delegate.getHardness();
    }

    @Override
    public boolean isHarvestPickaxe() {
        return Registries.createEntryLookup(Registries.BLOCK).getOptional(BlockTags.PICKAXE_MINEABLE).map(a -> a.contains(
                Registries.BLOCK.getEntry(delegate))).orElse(false);
    }

    @Override
    public boolean isHarvestAxe() {
        return Registries.createEntryLookup(Registries.BLOCK).getOptional(BlockTags.AXE_MINEABLE).map(a -> a.contains(
                Registries.BLOCK.getEntry(delegate))).orElse(false);
    }

    @Override
    public boolean isHarvestShovel() {
        return Registries.createEntryLookup(Registries.BLOCK).getOptional(BlockTags.SHOVEL_MINEABLE).map(a -> a.contains(
                Registries.BLOCK.getEntry(delegate))).orElse(false);
    }
}
