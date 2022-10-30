package kr.syeyoung.dungeonsguide.mod.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BlockCache {
    public BlockCache() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    private final LoadingCache<BlockPos, IBlockState> cache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .build(
                    new CacheLoader<BlockPos, IBlockState>() {
                        public IBlockState load(@NotNull BlockPos pos) {
                            return Minecraft.getMinecraft().theWorld.getBlockState(pos);
                        }
                    });


    @SuppressWarnings("UnstableApiUsage")
    public IBlockState getBlockState(@NotNull BlockPos pos){
        if(FeatureRegistry.DEBUG_BLOCK_CACHING.isEnabled()){
            return cache.getUnchecked(pos);
        } else {
            return Minecraft.getMinecraft().theWorld.getBlockState(pos);
        }
    }


    @SubscribeEvent @SuppressWarnings("UnstableApiUsage")
    public void onWorldLoad(WorldEvent.Load e){
        cache.invalidateAll();
    }


}
