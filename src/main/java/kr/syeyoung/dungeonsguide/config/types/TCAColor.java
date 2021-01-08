package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.awt.*;

public class TCAColor implements TypeConverter<Color> {
    @Override
    public String getTypeString() {
        return "color";
    }

    @Override
    public Color deserialize(JsonElement element) {
        return new Color(element.getAsInt());
    }

    @Override
    public JsonElement serialize(Color element) {
        return new JsonPrimitive(element.getRGB());
    }
}
