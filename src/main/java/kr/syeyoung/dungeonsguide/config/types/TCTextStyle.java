package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
        TextStyle textStyle = new TextStyle();
        textStyle.setColor(TypeConverterRegistry.getTypeConverter("acolor", AColor.class).deserialize(element.getAsJsonObject().get("color")));
        textStyle.setGroupName(element.getAsJsonObject().get("group").getAsString());
        return textStyle;
    }

    @Override
    public JsonElement serialize(TextStyle element) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("color", TypeConverterRegistry.getTypeConverter("acolor", AColor.class).serialize(element.getColor()));
        jsonObject.addProperty("group", element.getGroupName());
        return jsonObject;
    }
}
