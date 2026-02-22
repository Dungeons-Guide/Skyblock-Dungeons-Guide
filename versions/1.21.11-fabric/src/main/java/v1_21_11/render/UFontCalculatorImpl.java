package v1_21_11.render;

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
}
