package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;

public interface IPathDisplayEngine<T extends IPathDisplayEngineConfiguration> {
    void renderActionRoute(UWorldRenderContext context, float partialTicks);

    ActionRoute getActionRoute();


    void tick();

    T getSettings();
    void setSettings(T settings);
}
