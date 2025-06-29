package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import kr.syeyoung.dungeonsguide.mod.dungeon.world.ArrayBackedBlockMap;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.world.BlockType;
import net.minecraft.nbt.NBTTagCompound;

public class SchematicLoader {
    public static ArrayBackedBlockMap loadSchematic(NBTTagCompound compound) {
        ArrayBackedBlockMap blockMap = new ArrayBackedBlockMap(0,0,0, compound.getShort("Width"), compound.getShort("Height"), compound.getShort("Length"));
        byte[] blocks = compound.getByteArray("Blocks");
        byte[] meta = compound.getByteArray("Data");
        for (int x = 0; x < compound.getShort("Width"); x++) {
            for (int y = 0; y < compound.getShort("Height"); y++) {
                for (int z = 0; z < compound.getShort("Length"); z++) {
                    int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
                    blockMap.setBlock(x, y, z, ModAPI.getAPI().getBlockRegistry().fromOldId(
                            ((blocks[index] & 0xFF) << 4) | (meta[index] & 0xF)
                    ));
                }
            }
        }
        return blockMap;
    }
    public static ArrayBackedBlockMap loadSchematicWithShape(NBTTagCompound compound, short shape) {
        ArrayBackedBlockMap blockMap = new ArrayBackedBlockMap(0,0,0, compound.getShort("Width"), compound.getShort("Height"), compound.getShort("Length"));
        byte[] blocks = compound.getByteArray("Blocks");
        byte[] meta = compound.getByteArray("Data");
        for (int x = 0; x < compound.getShort("Width"); x++) {
            for (int y = 0; y < compound.getShort("Height"); y++) {
                for (int z = 0; z < compound.getShort("Length"); z++) {
                    if (!( (shape >>((z/32) *4 +(x/32)) & 0x1) > 0)) {
                        blockMap.setBlock(x,y,z, ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.AIR));
                        continue;
                    }

                    int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");

                    blockMap.setBlock(x, y, z, ModAPI.getAPI().getBlockRegistry().fromOldId(
                            ((blocks[index] & 0xFF) << 4) | (meta[index] & 0xF)
                    ));
                }
            }
        }
        return blockMap;
    }

}
