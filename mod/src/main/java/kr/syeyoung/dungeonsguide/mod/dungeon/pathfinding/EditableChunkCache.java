/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EditableChunkCache extends ChunkCache {
    public EditableChunkCache(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn) {
        super(worldIn, posFromIn, posToIn, subIn);
    }

    public void updateChunk(Chunk c) {
        int xWidth = this.chunkArray.length;
        int zWidth = this.chunkArray[0].length;
        int localX = c.xPosition - this.chunkX;
        int localZ = c.zPosition - this.chunkZ;
        if (localZ < 0 || localX < 0 || localX >= xWidth || localZ >= zWidth) throw new IllegalArgumentException("This "+c.xPosition+"/"+c.zPosition+" chunk doesn't belong here "+this.chunkX+"/"+this.chunkZ+" width "+xWidth+"x"+zWidth);
        this.chunkArray[localX][localZ] = c;
    }
    public void updateChunk(BlockPos p) {
        updateChunk(this.worldObj.getChunkFromBlockCoords(p));
    }
}
