package kr.syeyoung.dungeonsguide.features.text;

import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.MParameter;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class PanelTextParameterConfig extends MPanel {

    private TextHUDFeature feature;

    @Override
    public void onBoundsUpdate() {
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    private GuiConfig config;
    public PanelTextParameterConfig(final GuiConfig config, TextHUDFeature feature) {
        this.config = config;
        for (FeatureParameter parameter: feature.getParameters()) {
            if (!parameter.getKey().equalsIgnoreCase("textStyles"))
                add(new MParameter(feature, parameter, config));
        }
        setBackgroundColor(new Color(38, 38, 38, 255));
    }


    MPanel within;
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        int heights = 100;
        within = null;
        for (MPanel panel:getChildComponents()) {
            panel.setPosition(new Point(5, -offsetY + heights + 5));
            heights += panel.getBounds().height;

            if (panel.getBounds().contains(relMousex0,relMousey0)) within = panel;
        }
        renderStyleEdit(absMousex, absMousey, relMousex0, relMousey0, partialTicks, scissor);
        if (within instanceof MParameter) {
            FeatureParameter feature = ((MParameter) within).getParameter();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            FeatureEditPane.drawHoveringText(new ArrayList<String>(Arrays.asList(feature.getDescription().split("\n"))), relMousex0,relMousey0, Minecraft.getMinecraft().fontRendererObj);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }
    }

    public void renderStyleEdit(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -offsetY, 0);
        List<StyledText> texts = feature.getDummyText();
        Map<String, TextStyle> styles = feature.getStylesMap();
        List<TextHUDFeature.StyleTextAssociated> calc = feature.calculate(texts, 5,5, styles);
        for (TextHUDFeature.StyleTextAssociated calc3: calc) {
            if (calc3.getRectangle().contains(relMousex0, relMousey0)) {
                for (TextHUDFeature.StyleTextAssociated calc2 : calc) {
                    if (calc2.getStyledText() == calc3.getStyledText())
                        Gui.drawRect(calc2.getRectangle().x, calc2.getRectangle().y, calc2.getRectangle().x + calc2.getRectangle().width, calc2.getRectangle().y + calc2.getRectangle().height, 0xFF5E5E5E);
                }
                break;
            }
        }
        feature.drawTextWithStylesAssociated(texts, 5,5, styles);

        // draw actual logic
        GlStateManager.popMatrix();
    }


    public int offsetY = 0;

    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (scrollAmount > 0) offsetY -= 20;
        else if (scrollAmount < 0) offsetY += 20;
        if (offsetY < 0) offsetY = 0;
    }

}
