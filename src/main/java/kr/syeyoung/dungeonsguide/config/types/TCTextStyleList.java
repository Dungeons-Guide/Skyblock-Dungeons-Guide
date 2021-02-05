package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;

import java.util.ArrayList;
import java.util.List;

public class TCTextStyleList implements TypeConverter<List<TextStyle>> {
    @Override
    public String getTypeString() {
        return "list_textStyle";
    }

    @Override
    public List<TextStyle>  deserialize(JsonElement element) {
        JsonArray arr = element.getAsJsonArray();
        TypeConverter<TextStyle> conv = TypeConverterRegistry.getTypeConverter("textstyle", TextStyle.class);
        List<TextStyle> texts = new ArrayList<TextStyle>();
        for (JsonElement element2:arr) {
            texts.add(conv.deserialize(element2));
        }
        return texts;
    }

    @Override
    public JsonElement serialize(List<TextStyle> element) {
        JsonArray array = new JsonArray();
        TypeConverter<TextStyle> conv = TypeConverterRegistry.getTypeConverter("textstyle", TextStyle.class);
        for (TextStyle st:element) {
            array.add(conv.serialize(st));
        }
        return array;
    }
}
