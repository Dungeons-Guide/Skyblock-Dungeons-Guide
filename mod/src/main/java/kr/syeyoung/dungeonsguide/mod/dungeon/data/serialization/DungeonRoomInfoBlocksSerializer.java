package kr.syeyoung.dungeonsguide.mod.dungeon.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DungeonRoomInfoBlocksSerializer extends StdSerializer<int[][]> {
    protected DungeonRoomInfoBlocksSerializer() {
        super(int[][].class);
    }

    @Override
    public void serialize(int[][] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartArray();
        gen.writeNumber(value.length);
        gen.writeNumber(value[0].length);

        // D111: format 2 writes palette indices as 16 bit, marked by a 4th element. format 1 was one byte each
        ByteArrayOutputStream baos = new ByteArrayOutputStream(value.length * value[0].length * 2);
        for (int[] ints : value) {
            for (int anInt : ints) {
                baos.write((anInt >> 8) & 0xFF);
                baos.write(anInt & 0xFF);
            }
        }
        gen.writeBinary(baos.toByteArray());
        gen.writeNumber(2);

        gen.writeEndArray();
    }
}
