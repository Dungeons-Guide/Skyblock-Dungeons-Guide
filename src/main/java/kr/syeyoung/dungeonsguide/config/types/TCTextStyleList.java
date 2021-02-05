package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

public class TCTextStyleList implements TypeConverter<List> {
    @Override
    public String getTypeString() {
        return "list_textStyle";
    }

    @Override
    public List deserialize(JsonElement element) {
        return new Color(element.getAsInt());
    }

    @Override
    public JsonElement serialize(List element) {
        return new JsonPrimitive(element.getRGB());
    }
}
