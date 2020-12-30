package kr.syeyoung.dungeonsguide.roomedit.panes;

import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionTree;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.elements.*;
import kr.syeyoung.dungeonsguide.roomprocessor.ProcessorFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class ActionDisplayPane extends MPanel {
    private DungeonRoom dungeonRoom;

    private ActionTreeDisplayPane displayPane;

    private MTextField textField;
    private MButton calculate;
    public ActionDisplayPane(final DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;

        {
            textField = new MTextField();
            textField.setBounds(new Rectangle(0,0,getBounds().width - 100, 20));
            add(textField);
        }
        {
            calculate = new MButton();
            calculate.setBounds(new Rectangle(getBounds().width - 100,0,100, 20));
            calculate.setText("calculate");
            calculate.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    try {
                        remove(displayPane);

                        String text = textField.getText();
                        String target = text.split(":")[0];
                        String state = text.split(":")[1];
                        ActionChangeState actionChangeState = new ActionChangeState(target, state);
                        ActionTree tree= ActionTree.buildActionTree(actionChangeState, dungeonRoom);

                        displayPane = new ActionTreeDisplayPane(dungeonRoom, tree);
                        displayPane.setBounds(new Rectangle(0,25,getBounds().width,getBounds().height-25));
                        add(displayPane);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });
            add(calculate);
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,5,parentWidth-10,parentHeight-10));
    }

    @Override
    public void onBoundsUpdate() {
        textField.setBounds(new Rectangle(0,0,getBounds().width - 100, 20));
        calculate.setBounds(new Rectangle(getBounds().width - 100,0,100, 20));
    }
}
