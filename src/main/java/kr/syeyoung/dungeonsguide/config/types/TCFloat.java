package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class TCFloat implements TypeConverter<Float> {
    @Override
    public String getTypeString() {
        return "float";
    }

    @Override
    public Float deserialize(JsonElement element) {
        return element.getAsFloat();
    }

    @Override
    public JsonElement serialize(Float element) {
        return new JsonPrimitive(element);
    }
}
