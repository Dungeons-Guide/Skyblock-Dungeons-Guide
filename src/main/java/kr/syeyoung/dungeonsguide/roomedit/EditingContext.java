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

package kr.syeyoung.dungeonsguide.roomedit;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonRoomEdit;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import java.util.LinkedList;
import java.util.Stack;

public class EditingContext {

    private static EditingContext editingContext;

    public static void createEditingContext(DungeonRoom dungeonRoom) {
        editingContext = new EditingContext(dungeonRoom);
    }

    public static EditingContext getEditingContext() {
        return editingContext;
    }

    public static void endEditingSession() {
        editingContext = null;
        Minecraft.getMinecraft().displayGuiScreen(null);
    }


    private EditingContext(DungeonRoom dungeonRoom) {
        this.room = dungeonRoom;
    }

    @Getter
    private final DungeonRoom room;

    @Getter
    private final Stack<GuiScreen> guiStack = new Stack<GuiScreen>();

    public boolean isEditingSecrets() {
        return guiDungeonRoomEdit.isEditingSelected();
    }
    public void endEditing() {
        guiDungeonRoomEdit.endEditing();
    }

    private GuiDungeonRoomEdit guiDungeonRoomEdit;
    @Getter
    private GuiScreen current;

    public void openGui(GuiScreen gui) {
        if (gui instanceof GuiDungeonRoomEdit) guiDungeonRoomEdit = (GuiDungeonRoomEdit) gui;
        guiStack.push(current);
        this.current = gui;
        Minecraft.getMinecraft().displayGuiScreen(gui);
    }

    public void goBack() {
        current = guiStack.pop();
        Minecraft.getMinecraft().displayGuiScreen(current);
    }

    public void reopen() {
        Minecraft.getMinecraft().displayGuiScreen(current);
    }
}
