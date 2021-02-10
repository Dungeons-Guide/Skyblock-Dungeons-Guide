package kr.syeyoung.dungeonsguide.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import lombok.Getter;

import java.util.List;

public class ActionRoute {
    @Getter
    private int current;
    @Getter
    private List<Action> actions;

    public ActionRoute(ActionTree tree) {
        actions = ActionTreeUtil.linearifyActionTree(tree);
        current = 0;
    }

    public Action next() {
        current ++;
        if (current >= actions.size()) current = actions.size() - 1;
        return actions.get(current);
    }

    public Action prev() {
        current --;
        if (current < 0) current = 0;
        return actions.get(current);
    }

    public Action getCurrentAction() {
        return actions.get(current);
    }
}
