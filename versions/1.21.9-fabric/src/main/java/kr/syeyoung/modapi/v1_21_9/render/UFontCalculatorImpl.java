package kr.syeyoung.modapi.v1_21_9.render;

import kr.syeyoung.modapi.rendering.TextStyleConfig;
import kr.syeyoung.modapi.rendering.UFontCalculator;
import net.minecraft.client.MinecraftClient;

public class UFontCalculatorImpl implements UFontCalculator {
    public static final UFontCalculatorImpl INSTANCE = new UFontCalculatorImpl();
    private UFontCalculatorImpl() {}

    @Override
    public int getFontHeight() {
        return MinecraftClient.getInstance().textRenderer.fontHeight;
    }

    @Override
    public int getStringWidth(String text) {
        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    @Override
    public double getCharWidth(char character, TextStyleConfig style) {
        return MinecraftClient.getInstance().textRenderer.getWidth(character+""); // TODO: proper $$
    }

    @Override
    public double getBaselineHeight(TextStyleConfig style) {
        return 0;
    }

    @Override
    public double getStringWidth(String text, TextStyleConfig style) {
        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }
}
