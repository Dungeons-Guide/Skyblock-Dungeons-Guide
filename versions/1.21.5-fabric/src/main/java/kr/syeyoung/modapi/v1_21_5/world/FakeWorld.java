package kr.syeyoung.modapi.v1_21_5.world;

import kr.syeyoung.modapi.world.IBlockAccessible;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class FakeWorld implements BlockView {
    @Getter @Setter
    private IBlockAccessible accessible;

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return ((UBlockStateImpl)accessible.getBlockStateAt(pos.getX(), pos.getY(), pos.getZ())).getDelegate();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight() {
        return 256;
    }

    @Override
    public int getBottomY() {
        return 0;
    }
}
