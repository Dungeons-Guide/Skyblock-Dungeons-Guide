package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public abstract class SpecialGuiScreen extends GuiScreen {

    private Runnable onDismiss;
    public void setOnDismiss(Runnable dismissed) {
        this.onDismiss = dismissed;
    }

    protected void dismiss() {
        onDismiss.run();
        Minecraft.getMinecraft().displayGuiScreen(null);
    }
}
