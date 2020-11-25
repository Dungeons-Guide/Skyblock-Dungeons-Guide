package kr.syeyoung.dungeonsguide.roomedit;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.Getter;
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
    private DungeonRoom room;

    private Stack<GuiScreen> guiStack = new Stack<GuiScreen>();

    @Getter
    private GuiScreen current;

    public void openGui(GuiScreen gui) {
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
