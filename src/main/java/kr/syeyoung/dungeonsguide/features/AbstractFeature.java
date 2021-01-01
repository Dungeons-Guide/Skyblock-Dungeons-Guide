package kr.syeyoung.dungeonsguide.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.config.types.TypeConverter;
import kr.syeyoung.dungeonsguide.config.types.TypeConverterRegistry;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public abstract class AbstractFeature {
    @Getter
    private final String category;
    @Getter
    private final String name;

    @Getter
    private final String description;

    @Getter
    private final String key;

    protected Map<String, FeatureParameter> parameters = new HashMap<String, FeatureParameter>();

    protected AbstractFeature(String category, String name, String description, String key) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.key = key;
    }

    @Getter
    @Setter
    private boolean enabled = true;

    public List<FeatureParameter> getParameters() { return new ArrayList<FeatureParameter>(parameters.values()); }

    public <T> FeatureParameter<T> getParameter(String key) {
        return parameters.get(key);
    }

    public void loadConfig(JsonObject jsonObject) { // gets key, calls it
        enabled = jsonObject.get("$enabled").getAsBoolean();
        for (Map.Entry<String, FeatureParameter> parameterEntry : parameters.entrySet()) {
            JsonElement element = jsonObject.get(parameterEntry.getKey());
            if (element == null) continue;
            TypeConverter typeConverter = TypeConverterRegistry.getTypeConverter(parameterEntry.getValue().getValue_type());
            parameterEntry.getValue().setValue(typeConverter.deserialize(element));
        }
    }

    public JsonObject saveConfig() {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, FeatureParameter> parameterEntry : parameters.entrySet()) {
            TypeConverter typeConverter = TypeConverterRegistry.getTypeConverter(parameterEntry.getValue().getValue_type());
            JsonElement obj = typeConverter.serialize(parameterEntry.getValue().getValue());
            object.add(parameterEntry.getKey(), obj);
        }
        object.addProperty("$enabled", isEnabled());
        return object;
    }
}
