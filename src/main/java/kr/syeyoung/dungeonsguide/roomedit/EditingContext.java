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
