package kr.syeyoung.dungeonsguide.mod.dungeon.data.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DungeonRoomInfoBlocksDeserializer extends StdDeserializer<int[][]> {
    protected DungeonRoomInfoBlocksDeserializer() {
        super(int[][].class);
    }

    @Override
    public int[][] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        ArrayNode node = p.getCodec().readTree(p);
        int length  = node.get(0).asInt();
        int width = node.get(1).asInt();
        byte[] bytes = node.get(2).binaryValue();

        int[][] arr = new int[length][width];
        if (node.size() > 3 && node.get(3).asInt() == 2) {
            // format 2, 16 bit palette indices
            for (int i = 0; i < bytes.length / 2; i++) {
                int value = (bytes[i * 2] & 0xFF) << 8 | (bytes[i * 2 + 1] & 0xFF);
                arr[i / width][i % width] = value == 0xFFFF ? -1 : value;
            }
            return arr;
        }
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == -1) arr[i/width][i%width] = -1;
            else arr[i / width][i % width] = bytes[i] & 0xFF;
        }

        return arr;
    }

}
