package kr.syeyoung.dungeonsguide.features.text;

import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.MParameter;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.gui.elements.MColor;
import kr.syeyoung.dungeonsguide.gui.elements.MEditableAColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PanelTextParameterConfig extends MPanel {

    private TextHUDFeature feature;

    private MEditableAColor currentColor;

    @Override
    public void onBoundsUpdate() {
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,5,parentWidth-10, parentHeight-10));
    }

    private GuiConfig config;
    public PanelTextParameterConfig(final GuiConfig config, final TextHUDFeature feature) {
        this.config = config;
        this.feature = feature;
        setBackgroundColor(new Color(38, 38, 38, 255));

        currentColor = new MEditableAColor();
        currentColor.setColor(new AColor(0xff555555, true));
        currentColor.setEnableEdit(false);
        currentColor.setSize(new Dimension(15, 10));
        currentColor.setBounds(new Rectangle(275 , 14, 15, 10));
        currentColor.setOnUpdate(new Runnable() {
            @Override
            public void run() {
                for (String se:selected)
                    feature.getStylesMap().get(se).setColor(currentColor.getColor());
            }
        });
        add(currentColor);
    }

    private Set<String> selected = new HashSet<String>();

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        GlStateManager.pushMatrix();

        int width = 200, height = 100;
        Gui.drawRect(0,0,getBounds().width, getBounds().height, 0xFF444444);
        Gui.drawRect(4,4,width+6, height+6, 0xFF222222);
        Gui.drawRect(5,5,width+5, height+5, 0xFF555555);
        Rectangle clip = new Rectangle(scissor.x + 5, scissor.y + 5, width, height);
        clip(new ScaledResolution(Minecraft.getMinecraft()), clip.x, clip.y, clip.width, clip.height);

        List<StyledText> texts = feature.getDummyText();
        Map<String, TextStyle> styles = feature.getStylesMap();
        List<TextHUDFeature.StyleTextAssociated> calc = feature.calculate(texts, 5,5, styles);
        boolean bool =clip.contains(absMousex, absMousey);
        for (TextHUDFeature.StyleTextAssociated calc3: calc) {
            if (selected.contains(calc3.getStyledText().getGroup())) {
                Gui.drawRect(calc3.getRectangle().x, calc3.getRectangle().y, calc3.getRectangle().x + calc3.getRectangle().width, calc3.getRectangle().y + calc3.getRectangle().height, 0xFF44A800);
            } else if (bool && calc3.getRectangle().contains(relMousex0, relMousey0)) {
                for (TextHUDFeature.StyleTextAssociated calc2 : calc) {
                    if (calc2.getStyledText().getGroup().equals(calc3.getStyledText().getGroup()))
                        Gui.drawRect(calc2.getRectangle().x, calc2.getRectangle().y, calc2.getRectangle().x + calc2.getRectangle().width, calc2.getRectangle().y + calc2.getRectangle().height, 0xFF777777);
                }
            }
        }
        feature.drawTextWithStylesAssociated(texts, 5,5, styles);
        clip(new ScaledResolution(Minecraft.getMinecraft()), scissor.x, scissor.y, scissor.width, scissor.height);


        GlStateManager.translate(5, height + 7, 0);
        GlStateManager.scale(0.5,0.5,0);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("Press Shift to multi-select", 0, 0, 0xFFBFBFBF);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(width + 15, 5, 0);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5,0.5,0);
        fr.drawString("Selected Groups: "+selected, 0, 0, 0xFFBFBFBF);
        GlStateManager.popMatrix();
        fr.drawString("Text Color: ", 0, 10, 0xFFFFFFFF);

        GlStateManager.popMatrix();
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        List<StyledText> texts = feature.getDummyText();
        Map<String, TextStyle> styles = feature.getStylesMap();
        boolean existed = selected.isEmpty();
        boolean found = false;
        List<TextHUDFeature.StyleTextAssociated> calc = feature.calculate(texts, 5,5, styles);
        for (TextHUDFeature.StyleTextAssociated calc3: calc) {
            if (calc3.getRectangle().contains(relMouseX, relMouseY)) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                    if (!selected.contains(calc3.getStyledText().getGroup()))
                        selected.add(calc3.getStyledText().getGroup());
                    else
                        selected.remove(calc3.getStyledText().getGroup());
                } else {
                    selected.clear();
                    selected.add(calc3.getStyledText().getGroup());
                }
                found = true;
            }
        }

        if (!found && !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) && relMouseX >= 5 && relMouseX <= 205 && relMouseY >= 5 && relMouseY <= 105) {
            selected.clear();
        }
        currentColor.setEnableEdit(selected.size() != 0);
        if (existed != selected.isEmpty()) {
            if (selected.size() != 0)
                currentColor.setColor(styles.get(selected.iterator().next()).getColor());
            else
                currentColor.setColor(new AColor(0xff555555, true));
        }

        if (selected.size() == 1)
            currentColor.setColor(styles.get(selected.iterator().next()).getColor());
    }
}
