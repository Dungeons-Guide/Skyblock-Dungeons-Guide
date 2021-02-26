package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;

public class TCGUIRectangle implements TypeConverter<GUIRectangle> {
    @Override
    public String getTypeString() {
        return "guirect";
    }

    @Override
    public GUIRectangle deserialize(JsonElement element) {
        if (element == null) return null;
        GUIRectangle rectangle = new GUIRectangle();
        rectangle.setX(((JsonObject)element).get("x").getAsDouble());
        rectangle.setY(((JsonObject)element).get("y").getAsDouble());
        rectangle.setWidth(((JsonObject)element).get("width").getAsDouble());
        rectangle.setHeight(((JsonObject)element).get("height").getAsDouble());
        return rectangle;
    }

    @Override
    public JsonElement serialize(GUIRectangle element) {
        JsonObject object = new JsonObject();
        object.addProperty("x", element.getX());
        object.addProperty("y", element.getY());
        object.addProperty("width", element.getWidth());
        object.addProperty("height", element.getHeight());
        return object;
    }
}
