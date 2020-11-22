package kr.syeyoung.dungeonsguide.dungeon.data;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

@Getter
public class DungeonDoor {
    private final World w;
    private final BlockPos position;
    private boolean exist = true;
    private boolean isZDir;

    private static final Set<Block> legalBlocks = Sets.newHashSet(Blocks.coal_block, Blocks.barrier, Blocks.monster_egg, Blocks.air, Blocks.hardened_clay);


    public DungeonDoor(World world, BlockPos pos) {
        this.w = world;
        this.position = pos;
        for (int x = -1; x<=1; x++)
            for (int y = -1; y<=1; y++)
                for (int z = -1; z<=1; z++) {
                    BlockPos pos2 = pos.add(x,y,z);
                    Block block = world.getChunkFromBlockCoords(pos2).getBlock(pos2);
                    if (!legalBlocks.contains(block)) exist = false;
                }
        if (exist) {
            BlockPos ZCheck = pos.add(0,0,2);
            isZDir = world.getChunkFromBlockCoords(ZCheck).getBlock(ZCheck) == Blocks.air;
        }
    }
}
