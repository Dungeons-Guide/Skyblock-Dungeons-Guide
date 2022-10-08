/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.roomedit.panes;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.gui.elements.MTextField;

import java.awt.*;
import java.util.UUID;

public class RoommatchingPane extends MPanel {
    private final DungeonRoom dungeonRoom;

    private RoomMatchDisplayPane displayPane;

    private final MTextField textField;
    private final MButton calculate;
    public RoommatchingPane(final DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;

        {
            textField = new MTextField();
            textField.setBounds(new Rectangle(0,0,getBounds().width - 100, 20));
            add(textField);
        }
        {
            calculate = new MButton();
            calculate.setBounds(new Rectangle(getBounds().width - 100,0,100, 20));
            calculate.setText("match");
            calculate.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    try {
                        remove(displayPane);

                        String text = textField.getText();
                        String target = text.split(":")[0];
                        String state = text.split(":")[1];

                        UUID uid = UUID.fromString(target);
                        int rotation = Integer.parseInt(state) % 4;


                        displayPane = new RoomMatchDisplayPane(dungeonRoom, uid, rotation);
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
