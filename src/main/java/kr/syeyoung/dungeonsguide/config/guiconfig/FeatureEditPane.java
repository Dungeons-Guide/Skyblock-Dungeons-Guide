package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class FeatureEditPane extends MPanel {
    private List<AbstractFeature> features;

    private List<MFeature> le = new ArrayList<MFeature>();

    private GuiConfig config;

    private MTextField textField;
    private String search = "";

    public FeatureEditPane(List<AbstractFeature> features, GuiConfig config) {
        this.features = features;
        this.config = config;
        buildElements();

    }


    public void buildElements() {
        for (AbstractFeature feature : features) {
            MFeature mFeature = new MFeature(feature, config);
            mFeature.setHover(new Color(94, 94, 94, 255));
            le.add(mFeature);
            add(mFeature);
        }

        textField = new MTextField() {
            @Override
            public void edit(String str) {
                offsetY = 0;
                search = str;
            }
        };
        textField.setText("");
        textField.setBounds(new Rectangle(getBounds().width - 200, 0, 200, 20));
        add(textField);
    }
    @Override
    public void onBoundsUpdate() {
        for (MPanel panel :getChildComponents()){
            panel.setSize(new Dimension(getBounds().width, panel.getPreferredSize().height));
        }
        textField.setBounds(new Rectangle(getBounds().width - 200, 0, 200, 20));
    }
    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,1,parentWidth-10,parentHeight-2));
    }

    @Override
    public List<MPanel> getChildComponents() {
        List<MPanel> comp = new ArrayList<MPanel>();
        comp.add(textField);
        for (MFeature feature:le) {
            if (feature.getFeature().getName().toLowerCase().contains(search.toLowerCase()))
                comp.add(feature);
        }
        return comp;
    }

    private int offsetY = 0;
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        int heights = 0;
        for (MPanel panel:getChildComponents()) {
            panel.setPosition(new Point(panel.getBounds().x, -offsetY + heights));
            heights += panel.getBounds().height + 5;
        }
    }

    @Override
    public void render0(ScaledResolution resolution, Point parentPoint, Rectangle parentClip, int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) {
        super.render0(resolution, parentPoint, parentClip, absMousex, absMousey, relMousex0, relMousey0, partialTicks);
    }

    private static float zLevel = 0;
    public static void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font)
    {
        if (!textLines.isEmpty())
        {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int i = 0;

            for (String s : textLines)
            {
                int j = font.getStringWidth(s);

                if (j > i)
                {
                    i = j;
                }
            }

            int l1 = x + 12;
            int i2 = y - 12;
            int k = 8;

            if (textLines.size() > 1)
            {
                k += 2 + (textLines.size() - 1) * 10;
            }

            zLevel = 300.0F;
            int l = -267386864;
            drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, l, l);
            drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, l, l);
            drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, l, l);
            drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, l, l);
            drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, l, l);
            int i1 = 1347420415;
            int j1 = (i1 & 16711422) >> 1 | i1 & -16777216;
            drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, i1, j1);
            drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, i1, j1);
            drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, i1, i1);
            drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, j1, j1);

            for (int k1 = 0; k1 < textLines.size(); ++k1)
            {
                String s1 = (String)textLines.get(k1);
                font.drawStringWithShadow(s1, (float)l1, (float)i2, -1);

                if (k1 == 0)
                {
                    i2 += 2;
                }

                i2 += 10;
            }

            zLevel = 0.0F;
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }
    protected static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)right, (double)top, (double)zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)top, (double)zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)bottom, (double)zLevel).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos((double)right, (double)bottom, (double)zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (scrollAmount > 0) offsetY -= 20;
        else if (scrollAmount < 0) offsetY += 20;
        if (offsetY < 0) offsetY = 0;
    }
}
