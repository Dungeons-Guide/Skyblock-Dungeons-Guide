package kr.syeyoung.dungeonsguide.config.types;

import kr.syeyoung.dungeonsguide.roomedit.Parameter;

import java.util.HashMap;
import java.util.Map;

public class TypeConverterRegistry {
    private static Map<String, TypeConverter> typeConverterMap = new HashMap<String, TypeConverter>();

    public static void register(TypeConverter typeConverter) {
        typeConverterMap.put(typeConverter.getTypeString(), typeConverter);
    }

    public static TypeConverter getTypeConverter(String type_string) {
        return typeConverterMap.get(type_string);
    }
    public static <T> TypeConverter<T> getTypeConverter(String type_string, Class<T> t) {
        return (TypeConverter<T>)typeConverterMap.get(type_string);
    }

    static {
        register(new TCBoolean());
        register(new TCInteger());
        register(new TCRectangle());
        register(new TCGUIRectangle());
        register(new TCString());
        register(new TCColor());
        register(new TCFloat());
        register(new TCAColor());
        register(new TCTextStyleList());
        register(new TCTextStyle());
    }
}
