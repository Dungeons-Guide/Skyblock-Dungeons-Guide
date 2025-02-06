package kr.syeyoung.dungeonsguide.mod.dungeon.data.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DungeonRoomInfoWorldDeserializer extends StdDeserializer<char[]> {
    protected DungeonRoomInfoWorldDeserializer() {
        super(char[].class);
    }

    @Override
    public char[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        byte[] bytes = p.getBinaryValue();
        char[] chars = new char[bytes.length / 2];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ((bytes[i * 2] & 0xFF) << 8 | (bytes[i * 2 + 1] & 0xFF));
        }
        return chars;
    }

}
