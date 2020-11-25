package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

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

    private static final Set<Block> legalBlocks = Sets.newHashSet(Blocks.coal_block, Blocks.barrier, Blocks.monster_egg, Blocks.air, Blocks.stained_hardened_clay);

    private boolean requiresKey = false;
    private boolean opened = false;

    public DungeonDoor(World world, BlockPos pos) {
        this.w = world;
        this.position = pos;
        Block itshouldbeall = world.getChunkFromBlockCoords(pos).getBlock(pos);
        if (!legalBlocks.contains(itshouldbeall)) {
            exist = false;
            return;
        }
        for (int x = -1; x<=1; x++)
            for (int y = -1; y<=1; y++)
                for (int z = -1; z<=1; z++) {
                    BlockPos pos2 = pos.add(x,y,z);
                    Block block = world.getChunkFromBlockCoords(pos2).getBlock(pos2);
                    if (itshouldbeall != block) exist = false;
                }
        if (exist) {
            BlockPos ZCheck = pos.add(0,0,2);
            isZDir = world.getChunkFromBlockCoords(ZCheck).getBlock(ZCheck) == Blocks.air;

            if (isZDir) {
                for (int x = -1; x<=1; x++)
                    for (int y = -1; y<=1; y++)
                        for (int z = -2; z<=2; z+=4) {
                            BlockPos pos2 = pos.add(x,y,z);
                            Block block = world.getChunkFromBlockCoords(pos2).getBlock(pos2);
                            if (block != Blocks.air) exist = false;
                        }
            } else {
                for (int x = -2; x<=2; x+=4)
                    for (int y = -1; y<=1; y++)
                        for (int z = -1; z<=1; z++) {
                            BlockPos pos2 = pos.add(x,y,z);
                            Block block = world.getChunkFromBlockCoords(pos2).getBlock(pos2);
                            if (block != Blocks.air) exist = false;
                        }
            }
        }
        if (!exist) {
            isZDir = false;
            return;
        }

        if (itshouldbeall == Blocks.stained_hardened_clay || itshouldbeall == Blocks.coal_block) {
            requiresKey = true;
        } else if (itshouldbeall == Blocks.barrier) {
            opened = true;
        }
    }
}
