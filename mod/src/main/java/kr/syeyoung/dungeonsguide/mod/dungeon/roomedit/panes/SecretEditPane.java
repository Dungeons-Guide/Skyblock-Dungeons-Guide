/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.panes;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MParameter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SecretEditPane extends MPanel implements DynamicEditor {
    private final DungeonRoom dungeonRoom;

    private MButton save;
    private MButton create;
    private final List<MParameter> parameters = new ArrayList<MParameter>();

    private final List<String> allowedClasses = new ArrayList<String>();

    public SecretEditPane(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
        buildElements();

        for (String clazz : ValueEditRegistry.getClassesSupported()) {
            if (clazz.contains("mechanics") || clazz.equals("null")) {
                allowedClasses.add(clazz);
            }
        }
    }

    public void createNewMechanic(String uid, DungeonMechanic data) {
        MParameter parameter;
        parameters.add(parameter = new MParameter(new Parameter(uid, data, data), SecretEditPane.this));
        parameter.setBounds(new Rectangle(0,0,getBounds().width, 20));
        parameter.setParent(SecretEditPane.this);
    }

    public void buildElements() {
        {
            create = new MButton();
            create.setText("Create New Mechanic");
            create.setBackgroundColor(Color.cyan);
            create.setBounds(new Rectangle(0,0,100,20));
            create.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    createNewMechanic(UUID.randomUUID().toString(), null);
                }
            });

            save = new MButton();
            save.setText("Save");
            save.setBackgroundColor(Color.green);
            save.setBounds(new Rectangle(0,0,100,20));
            save.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    DungeonRoomInfo dungeonRoomInfo = dungeonRoom.getDungeonRoomInfo();
                    dungeonRoomInfo.getMechanics().clear();

                    for (MParameter parameter : parameters) {
                        Parameter real = parameter.getParameter();

                        ValueEditCreator vec = ValueEditRegistry.getValueEditMap(real.getNewData() == null ? "null" :real.getNewData().getClass().getName());

                        real.setPreviousData(vec.cloneObj(real.getNewData()));
                        dungeonRoomInfo.getMechanics().put(real.getName(), (DungeonMechanic) real.getNewData());
                    }
                }
            });
            create.setParent(this); save.setParent(this);
        }
        {
            for (Map.Entry<String, DungeonMechanic> en : dungeonRoom.getDungeonRoomInfo().getMechanics().entrySet()) {
                ValueEditCreator vec = ValueEditRegistry.getValueEditMap(en.getValue() == null ? "null" :en.getValue().getClass().getName());

                MParameter mParameter = new MParameter(new Parameter(en.getKey(), vec.cloneObj(en.getValue()), vec.cloneObj(en.getValue())), this);
                mParameter.setBounds(new Rectangle(0,0,getBounds().width,20));
                parameters.add(mParameter);
                mParameter.setParent(this);
            }
        }
    }

    @Override
    public void onBoundsUpdate() {
        for (MPanel panel :getChildComponents()){
            panel.setSize(new Dimension(getBounds().width, 20));
        }
    }
    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,5,parentWidth-10,parentHeight-10));
    }


    public void delete(MParameter parameter) {
        parameters.remove(parameter);
    }

    @Override
    public List<String> allowedClass() {
        return allowedClasses;
    }

    @Override
    public List<MPanel> getChildComponents() {
        ArrayList<MPanel> panels = new ArrayList<MPanel>(parameters);
        panels.add(create);
        panels.add(save);
        return panels;
    }

    private int offsetY = 0;
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        int heights = 0;
        for (MPanel panel:getChildComponents()) {
            panel.setPosition(new Point(0, -offsetY + heights));
            heights += panel.getBounds().height;
        }
    }

    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (scrollAmount > 0) offsetY -= 20;
        else if (scrollAmount < 0) offsetY += 20;
        if (offsetY < 0) offsetY = 0;
    }
}
