package kr.syeyoung.modapi.v1_8_9.render;

import kr.syeyoung.modapi.rendering.TextStyleConfig;
import kr.syeyoung.modapi.rendering.UFontCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class UFontCalculatorImpl implements UFontCalculator {
    public static final UFontCalculatorImpl INSTANCE = new UFontCalculatorImpl();
    private FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

    private UFontCalculatorImpl() {}

    @Override
    public int getFontHeight() {
        return fontRenderer.FONT_HEIGHT;
    }

    @Override
    public int getStringWidth(String text) {
        return fontRenderer.getStringWidth(text);
    }

    @Override
    public double getCharWidth(char character, TextStyleConfig style) {
        return DefaultFontRendererImpl.getInstance().getWidth(character, style);
    }

    @Override
    public double getBaselineHeight(TextStyleConfig style) {
        return DefaultFontRendererImpl.getInstance().getBaselineHeight(style);
    }

    @Override
    public double getStringWidth(String text, TextStyleConfig style) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        double width = 0;
        for (char c : text.toCharArray()) {
            width += getCharWidth(c, style);
        }
        return width;
    }
}
