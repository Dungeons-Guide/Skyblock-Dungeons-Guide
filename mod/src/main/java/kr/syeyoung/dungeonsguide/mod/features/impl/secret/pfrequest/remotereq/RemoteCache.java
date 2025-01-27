package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.remotereq;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class RemoteCache {
    private String requestId;
    private String name;
    private String linkedPreset;
    private boolean wasInProgress = true;
    private boolean checkedAfterComplete = false;

    public JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("requestId", requestId);
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("linkedPreset", linkedPreset);
        jsonObject.addProperty("inProgress", wasInProgress);
        jsonObject.addProperty("checked", checkedAfterComplete);
        return jsonObject;
    }

    public static RemoteCache fromJson(JsonObject jsonObject) {
        return new RemoteCache(jsonObject.get("requestId").getAsString(),
                jsonObject.get("name").getAsString(),
                jsonObject.get("linkedPreset").getAsString(),
                jsonObject.get("inProgress").getAsBoolean(),
                jsonObject.get("checked").getAsBoolean());
    }
}
