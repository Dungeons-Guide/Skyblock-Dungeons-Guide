package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class TCInteger implements TypeConverter<Integer> {
    @Override
    public String getTypeString() {
        return "integer";
    }

    @Override
    public Integer deserialize(JsonElement element) {
        return element.getAsInt();
    }

    @Override
    public JsonElement serialize(Integer element) {
        return new JsonPrimitive(element);
    }
}
