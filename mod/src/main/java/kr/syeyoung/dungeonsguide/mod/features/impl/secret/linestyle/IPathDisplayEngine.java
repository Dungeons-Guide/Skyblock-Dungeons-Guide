package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;

public interface IPathDisplayEngine<T> {
    void renderActionRoute(RenderWorldEvent event);

    ActionRoute getActionRoute();


    void tick();

    T getSettings();
    void setSettings(T settings);
}
