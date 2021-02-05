package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.awt.*;

public class TCAColor implements TypeConverter<AColor> {
    @Override
    public String getTypeString() {
        return "acolor";
    }

    @Override
    public AColor deserialize(JsonElement element) {
        if (element instanceof JsonPrimitive)
            return new AColor(element.getAsInt(), true);

        JsonObject object = element.getAsJsonObject();
        AColor color = new AColor(object.get("color").getAsInt(), true);
        color.setChroma(object.get("chroma").getAsBoolean());
        color.setChromaSpeed(object.get("chromaSpeed").getAsFloat());
        return color;
    }

    @Override
    public JsonElement serialize(AColor element) {
        JsonObject object = new JsonObject();
        object.addProperty("color", element.getRGB());
        object.addProperty("chroma", element.isChroma());
        object.addProperty("chromaSpeed", element.getChromaSpeed());
        return object;
    }
}
