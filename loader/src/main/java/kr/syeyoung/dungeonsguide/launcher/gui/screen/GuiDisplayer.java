package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;
import java.util.Queue;

public class GuiDisplayer {
    public static GuiDisplayer INSTANCE = new GuiDisplayer();
    private GuiDisplayer() {}

    private Queue<SpecialGuiScreen> guiScreensToShow = new LinkedList<>();
    private boolean isMcLoaded;


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent guiOpenEvent) {
        if (guiOpenEvent.gui instanceof GuiMainMenu) {
            isMcLoaded = true;
        }
        if (guiScreensToShow.size() > 0 && guiOpenEvent.gui != guiScreensToShow.peek()) {
            SpecialGuiScreen gui = guiScreensToShow.peek();
            if (gui == null) return;
            gui.setOnDismiss(guiScreensToShow::poll);
            guiOpenEvent.gui = gui;
        }
    }

    public void displayGui(SpecialGuiScreen specialGuiScreen) {
        if (specialGuiScreen == null) return;
        if (!guiScreensToShow.contains(specialGuiScreen))
            guiScreensToShow.add(specialGuiScreen);
        if (isMcLoaded) {
            SpecialGuiScreen gui = guiScreensToShow.peek();
            if (gui == null) return;
            gui.setOnDismiss(guiScreensToShow::poll);
            Minecraft.getMinecraft().displayGuiScreen(gui);
        }
    }
}
