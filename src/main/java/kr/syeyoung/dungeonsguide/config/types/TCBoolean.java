package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class TCBoolean implements TypeConverter<Boolean> {
    @Override
    public String getTypeString() {
        return "boolean";
    }

    @Override
    public Boolean deserialize(JsonElement element) {
        return element.getAsBoolean();
    }

    @Override
    public JsonElement serialize(Boolean element) {
        return new JsonPrimitive(element);
    }
}
