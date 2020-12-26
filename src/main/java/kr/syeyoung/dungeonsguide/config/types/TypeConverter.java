package kr.syeyoung.dungeonsguide.config.types;

import com.google.gson.JsonElement;

public interface TypeConverter<T> {
    String getTypeString();

    T deserialize(JsonElement element);

    JsonElement serialize(T element);
}
