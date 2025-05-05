package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.config.types.FeatureTypeHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPathDisplayEngineSetting<T> implements PathDisplayEngineSetting<T> {

    protected Map<String, FeatureParameter> parameters = new HashMap<String, FeatureParameter>();

    protected void addParameter(String name, FeatureParameter f){
        parameters.put(name, f);
    }


    public List<FeatureParameter> getParameters() { return new ArrayList<FeatureParameter>(parameters.values()); }

    public <T> FeatureParameter<T> getParameter(String key) {
        return parameters.get(key);
    }

    public void deserialize(JsonObject jsonObject) { // gets key, calls it
        for (Map.Entry<String, FeatureParameter> parameterEntry : parameters.entrySet()) {
            parameterEntry.getValue().setToDefault();
            JsonElement element = jsonObject.get(parameterEntry.getKey());
            if (element == null) continue;
            FeatureTypeHandler featureTypeHandler = parameterEntry.getValue().getFeatureTypeHandler();
            try {
                parameterEntry.getValue().setValue(featureTypeHandler.deserialize(element));
            } catch (Exception e) {}
        }
    }

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, FeatureParameter> parameterEntry : parameters.entrySet()) {
            FeatureTypeHandler featureTypeHandler =  parameterEntry.getValue().getFeatureTypeHandler();
            JsonElement obj = featureTypeHandler.serialize(parameterEntry.getValue().getValue());
            object.add(parameterEntry.getKey(), obj);
        }
        return object;
    }


}
