package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.launcher.exceptions.auth.AuthFailedExeption;
import kr.syeyoung.dungeonsguide.launcher.exceptions.auth.PrivacyPolicyRequiredException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiPrivacyPolicy extends SpecialGuiScreen {
    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        int width = Math.min(300, sr.getScaledWidth() / 2 - 20);

        this.buttonList.add(new GuiButton(0, sr.getScaledWidth()/2 + 10,sr.getScaledHeight()-40, width, 20,"Accept Privacy Policy"));
        this.buttonList.add(new GuiButton(1, sr.getScaledWidth() / 2 - 10 - width,sr.getScaledHeight()-40, width, 20,"Deny and Play Without DG"));
    }


    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 0) {
            // accept
            try {
                AuthManager.getInstance().acceptPrivacyPolicy(1);
            } catch (Exception e) {
                e.printStackTrace();
//                GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
                // display tooltip.
            }
            dismiss();
        } else if (button.id == 1) {
            dismiss();
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(0);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        fontRenderer.drawString("Please accept or deny Dungeons Guide Privacy Policy to continue", (sr.getScaledWidth()-fontRenderer.getStringWidth("Please accept or deny Dungeons Guide Privacy Policy to continue"))/2,40,0xFFFF0000);
        fontRenderer.drawString("Blah blah legal stuff", (sr.getScaledWidth()-fontRenderer.getStringWidth("Please accept or deny Dungeons Guide Privacy Policy to continue"))/2,sr.getScaledHeight() / 2, 0xFFFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;

        int scale = resolution.getScaleFactor();
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }
}
