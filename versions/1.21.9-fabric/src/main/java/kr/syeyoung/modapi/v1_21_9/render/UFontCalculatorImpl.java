package kr.syeyoung.modapi.v1_21_9.render;

import kr.syeyoung.modapi.rendering.TextStyleConfig;
import kr.syeyoung.modapi.rendering.UFontCalculator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

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
        return  MinecraftClient.getInstance().textRenderer.getWidth(StringVisitable.styled(character+"", Style.EMPTY.withColor(style.getTextColor())
                .withShadowColor(style.getShadowColor())
                .withBold(style.isBold())
                .withItalic(style.isItalic())
                .withUnderline(style.isUnderline()))) * style.getSize() / 8.0; // TODO: proper $$
    }

    @Override
    public double getBaselineHeight(TextStyleConfig style) {
        return 7 * style.getSize() / 8.0;
    }

    @Override
    public double getStringWidth(String text, TextStyleConfig style) {
        return MinecraftClient.getInstance().textRenderer.getWidth(StringVisitable.styled(text, Style.EMPTY.withColor(style.getTextColor())
                .withShadowColor(style.getShadowColor())
                .withBold(style.isBold())
                .withItalic(style.isItalic())
                .withUnderline(style.isUnderline()))) * style.getSize() / 8.0;
    }
}
