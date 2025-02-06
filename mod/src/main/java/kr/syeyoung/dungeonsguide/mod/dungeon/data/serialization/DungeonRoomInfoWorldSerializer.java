package kr.syeyoung.dungeonsguide.mod.dungeon.data.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;

public class DungeonRoomInfoWorldSerializer extends StdSerializer<char[]> {
    protected DungeonRoomInfoWorldSerializer() {
        super(char[].class);
    }

    @Override
    public void serialize(char[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        byte[] bytes = new byte[value.length * 2];
        for (int i = 0; i < value.length; i++) {
            bytes[i * 2] = (byte) ((value[i] >> 8) & 0xFF);
            bytes[i*2 + 1] = (byte) (value[i] & 0xFF);
        }
        gen.writeBinary(bytes);
    }
}
