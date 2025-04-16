package kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.styles;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;

public interface IPathfindEngineFactory<T extends IPathDisplayEngine<U>, U> {
    T create(ActionRoute route, U settings);
}
