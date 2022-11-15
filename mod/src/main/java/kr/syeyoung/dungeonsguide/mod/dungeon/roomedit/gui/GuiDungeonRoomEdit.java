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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui;

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/roomedit/gui/GuiDungeonRoomEdit.java
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.gui.MGui;
import kr.syeyoung.dungeonsguide.gui.elements.MTabbedPane;
import kr.syeyoung.dungeonsguide.roomedit.panes.*;
import kr.syeyoung.dungeonsguide.roomedit.panes.*;
========
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.gui.MGui;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MTabbedPane;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.panes.*;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/dungeon/roomedit/gui/GuiDungeonRoomEdit.java
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class GuiDungeonRoomEdit extends MGui {

    private final DungeonRoom room;

    private final MTabbedPane tabbedPane;
    @Getter
    private final SecretEditPane sep;

    public GuiDungeonRoomEdit(DungeonRoom room) {
        this.room = room;

        MTabbedPane tabbedPane = new MTabbedPane();
        getMainPanel().add(tabbedPane);
        tabbedPane.setBackground2(new Color(17, 17, 17, 179));


        tabbedPane.addTab("General", new GeneralEditPane(room));
        tabbedPane.addTab("Match", new RoomDataDisplayPane(room));
        tabbedPane.addTab("Secrets", sep = new SecretEditPane(room));
        tabbedPane.addTab("Actions", new ActionDisplayPane(room));
        tabbedPane.addTab("Test", new RoommatchingPane(room));
        tabbedPane.addTab("Proc.Params", new ProcessorParameterEditPane(room));
        this.tabbedPane = tabbedPane;
    }

    public boolean isEditingSelected() {
        return "Secrets".equals(tabbedPane.getSelectedKey());
    }
    public void endEditing() {
        tabbedPane.setSelectedKey("General");
    }

    @Override
    public void initGui() {
        super.initGui();
        // update bounds
        getMainPanel().setBounds(new Rectangle(Math.min((Minecraft.getMinecraft().displayWidth - 500) / 2, Minecraft.getMinecraft().displayWidth), Math.min((Minecraft.getMinecraft().displayHeight - 300) / 2, Minecraft.getMinecraft().displayHeight),500,300));
    }
}
