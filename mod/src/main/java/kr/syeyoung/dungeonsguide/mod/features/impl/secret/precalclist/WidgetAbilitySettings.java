package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.AlgorithmSettings;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.FontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.List;

public class WidgetAbilitySettings extends Widget implements Renderer {
    private BindableAttribute<AlgorithmSettings> settings = new BindableAttribute<>(AlgorithmSettings.class);
    public WidgetAbilitySettings(BindableAttribute<AlgorithmSettings> settings) {
        this.settings.exportTo(settings);
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {

        AlgorithmSettings algorithmSettings = settings.getValue();

        Minecraft.getMinecraft().fontRendererObj.drawString("blahblah", 0, 0, 0xFFFFFFFF);
    }
}
