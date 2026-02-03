package kr.syeyoung.modapi.v1_8_9.render;

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
}
