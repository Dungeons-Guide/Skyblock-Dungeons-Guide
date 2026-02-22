package kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.fonts;

import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.FlatTextSpan;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.styles.ITextStyle;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.rendering.TextStyleConfig;

public class DefaultFontRenderer implements FontRenderer {
    public static DefaultFontRenderer DEFAULT_RENDERER = new DefaultFontRenderer();

    private DefaultFontRenderer() {}

    @Override
    public double getWidth(char text, ITextStyle textStyle) {
        TextStyleConfig config = TextStyleAdapter.toTextStyleConfig(textStyle);
        return ModAPI.getAPI().getFontCalculator().getCharWidth(text, config);
    }

    @Override
    public double getBaselineHeight(ITextStyle textStyle) {
        TextStyleConfig config = TextStyleAdapter.toTextStyleConfig(textStyle);
        return ModAPI.getAPI().getFontCalculator().getBaselineHeight(config);
    }

    @Override
    public void render(RenderingContext context, FlatTextSpan lineElement, double x, double y, double currentScale) {
        TextStyleConfig config = TextStyleAdapter.toTextStyleConfig(lineElement.textStyle);

        String text = new String(lineElement.value);
        context.ctx().drawStringWithStyle(text, x, y, config);
    }
}
