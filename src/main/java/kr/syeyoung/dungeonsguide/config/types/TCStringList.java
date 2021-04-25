package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TCStringList implements TypeConverter<List<String>> {
    @Override
    public String getTypeString() {
        return "stringlist";
    }

    @Override
    public List<String> deserialize(JsonElement element) {
        List<String> strList = new ArrayList<>();
        for (JsonElement jsonElement : element.getAsJsonArray()) {
            strList.add(jsonElement.getAsString());
        }
        return strList;
    }

    @Override
    public JsonElement serialize(List<String> element) {
        JsonArray jsonElements = new JsonArray();
        for (String s : element) {
            jsonElements.add(new JsonPrimitive(s));
        }
        return jsonElements;
    }
}
