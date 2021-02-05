package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;

import java.util.List;

public class TCTextStyle implements TypeConverter<TextStyle> {
    @Override
    public String getTypeString() {
        return "textstyle";
    }

    @Override
    public TextStyle deserialize(JsonElement element) {
        return new Color(element.getAsInt());
    }

    @Override
    public JsonElement serialize(TextStyle element) {
        return new JsonPrimitive(element.getRGB());
    }
}
