package kr.syeyoung.dungeonsguide.dungeon.serialization;

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

        ByteArrayOutputStream baos = new ByteArrayOutputStream(value.length * value[0].length);
        for (int[] ints : value) {
            for (int anInt : ints) {
                baos.write(anInt); // wait this waste 3 bytes!!! no, it doesn't.
            }
        }
        gen.writeBinary(baos.toByteArray());

        gen.writeEndArray();
    }
}
