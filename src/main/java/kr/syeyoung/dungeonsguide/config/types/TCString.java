package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class TCString implements TypeConverter<String> {
    @Override
    public String getTypeString() {
        return "string";
    }

    @Override
    public String deserialize(JsonElement element) {
        return element.getAsString();
    }

    @Override
    public JsonElement serialize(String element) {
        return new JsonPrimitive(element);
    }
}
