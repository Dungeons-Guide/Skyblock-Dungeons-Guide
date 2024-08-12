package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.GUIPosition;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import scala.actors.threadpool.Arrays;

import java.util.Collections;
import java.util.List;

public class WidgetMapDemo extends Widget implements Renderer {
    private FeatureDungeonMap2 featureDungeonMap2;
    private MapConfiguration mapConfiguration;
    public WidgetMapDemo(FeatureDungeonMap2 featureDungeonMap2) {
        this.featureDungeonMap2 = featureDungeonMap2;
        this.mapConfiguration = featureDungeonMap2.getMapConfiguration();
    }
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList( new WidgetDungeonMap(mapConfiguration, featureDungeonMap2::getOverlay));
    }


    @Override
    public void doRender(float partialTicks, RenderingContext renderCtx, DomElement buildContext) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (SkyblockStatus.isOnDungeon() && context != null && context.getScaffoldParser() != null) {
            renderCtx.pushClip(buildContext.getAbsBounds(), buildContext.getSize(), 0,0, buildContext.getSize().getWidth(), buildContext.getSize().getHeight());
            SingleChildRenderer.INSTANCE.doRender(partialTicks, renderCtx, buildContext);
            renderCtx.popClip();
            return;
        }
        renderCtx.pushClip(buildContext.getAbsBounds(), buildContext.getSize(), 0,0, buildContext.getSize().getWidth(), buildContext.getSize().getHeight());

        System.out.println(buildContext.getSize() );
        Size featureRect = getDomElement().getSize();
        Gui.drawRect(0, 0, (int) featureRect.getWidth(), (int) featureRect.getWidth(), RenderUtils.getColorAt(0,0, featureDungeonMap2.getMapConfiguration().getBackgroundColor()));
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Please join a dungeon to see preview", (int) featureRect.getWidth() / 2 - fr.getStringWidth("Please join a dungeon to see preview") / 2, (int) featureRect.getWidth() / 2 - fr.FONT_HEIGHT / 2, 0xFFFFFFFF);
        GL11.glLineWidth((float) mapConfiguration.getBorderWidth());
        RenderUtils.drawUnfilledBox(0, 0, (int) featureRect.getWidth(), (int) featureRect.getWidth(),mapConfiguration.getBorder());

        renderCtx.popClip();


    }
}
