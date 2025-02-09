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

package kr.syeyoung.dungeonsguide.mod.pathfinding.world;

import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class EditableChunkCache extends ChunkCache {
    public EditableChunkCache(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn) {
        super(worldIn, posFromIn, posToIn, subIn);
    }

    public boolean isManaged(int cx, int cz) {
        int xWidth = this.chunkArray.length;
        int zWidth = this.chunkArray[0].length;
        int localX = cx- this.chunkX;
        int localZ = cz - this.chunkZ;
        return localZ >= 0 && localX >= 0 && localX < xWidth && localZ < zWidth;
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
