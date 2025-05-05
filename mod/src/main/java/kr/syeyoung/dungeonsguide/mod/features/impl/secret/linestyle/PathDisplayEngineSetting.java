package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;

public interface PathDisplayEngineSetting<T> {
    IPathDisplayEngine<T> createPathDisplayEngine(ActionRoute route);
    PathDisplayEngineSettingRegistration getRegistration();

    Widget createSettingWidget();
    Widget createPreviewWidget();

    JsonObject serialize();

    void deserialize(JsonObject jsonObject);
}
