package kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.styles;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;

public interface IPathDisplayEngine<T> {
    void renderActionRoute(float partialTicks);

    ActionRoute getActionRoute();


    void tick();

    T getSettings();
    void setSettings(T settings);
}
