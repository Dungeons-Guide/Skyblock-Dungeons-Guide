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
        textStyle.setBackground(element.getAsJsonObject().has("background") ? TypeConverterRegistry.getTypeConverter("acolor", AColor.class).deserialize(element.getAsJsonObject().get("background"))
                : new AColor(0x00777777, true));
        textStyle.setGroupName(element.getAsJsonObject().get("group").getAsString());
        if (element.getAsJsonObject().has("shadow"))
        textStyle.setShadow(element.getAsJsonObject().get("shadow").getAsBoolean());
        return textStyle;
    }

    @Override
    public JsonElement serialize(TextStyle element) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("color", TypeConverterRegistry.getTypeConverter("acolor", AColor.class).serialize(element.getColor()));
        jsonObject.add("background", TypeConverterRegistry.getTypeConverter("acolor", AColor.class).serialize(element.getBackground()));
        jsonObject.addProperty("group", element.getGroupName());
        jsonObject.addProperty("shadow", element.isShadow());
        return jsonObject;
    }
}
