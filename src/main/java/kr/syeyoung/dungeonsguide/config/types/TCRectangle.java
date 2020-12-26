package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.w3c.dom.css.Rect;

import java.awt.*;

public class TCRectangle implements TypeConverter<Rectangle> {
    @Override
    public String getTypeString() {
        return "rect";
    }

    @Override
    public Rectangle deserialize(JsonElement element) {
        Rectangle rectangle = new Rectangle();
        rectangle.x = ((JsonObject)element).get("x").getAsInt();
        rectangle.y = ((JsonObject)element).get("y").getAsInt();
        rectangle.width = ((JsonObject)element).get("width").getAsInt();
        rectangle.height = ((JsonObject)element).get("height").getAsInt();
        return rectangle;
    }

    @Override
    public JsonElement serialize(Rectangle element) {
        JsonObject object = new JsonObject();
        object.addProperty("x", element.x);
        object.addProperty("y", element.y);
        object.addProperty("width", element.width);
        object.addProperty("height", element.height);
        return object;
    }
}
