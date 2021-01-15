package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.elements.MTabbedPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

public class GuiConfig extends GuiScreen {

    private MPanel mainPanel = new MPanel();

    private MTabbedPane tabbedPane;

    public GuiConfig() {
        MTabbedPane tabbedPane = new MTabbedPane();
        mainPanel.add(tabbedPane);
        tabbedPane.setBackground2(new Color(17, 17, 17, 179));

        for (Map.Entry<String, List<AbstractFeature>> cate: FeatureRegistry.getFeaturesByCategory().entrySet())
            if (!cate.getKey().equals("hidden"))
                tabbedPane.addTab(cate.getKey(), new FeatureEditPane(cate.getValue(), this));
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        mainPanel.setBounds(new Rectangle(Math.min((scaledResolution.getScaledWidth() - 500) / 2, scaledResolution.getScaledWidth()), Math.min((scaledResolution.getScaledHeight() - 300) / 2, scaledResolution.getScaledHeight()),500,300));
    }
    FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();GL11.glDisable(GL11.GL_FOG);
        GlStateManager.color(1,1,1,1);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        mainPanel.render0(scaledResolution, new Point(0,0), new Rectangle(0,0,scaledResolution.getScaledWidth(),scaledResolution.getScaledHeight()), mouseX, mouseY, mouseX, mouseY, partialTicks);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        mainPanel.keyTyped0(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        mainPanel.mouseClicked0(mouseX, mouseY,mouseX,mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        mainPanel.mouseReleased0(mouseX, mouseY,mouseX,mouseY, state);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        mainPanel.mouseClickMove0(mouseX,mouseY,mouseX,mouseY,clickedMouseButton,timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            mainPanel.mouseScrolled0(i, j,i,j, wheel);
        }
    }
}
