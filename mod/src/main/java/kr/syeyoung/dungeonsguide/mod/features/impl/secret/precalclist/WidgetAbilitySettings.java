package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class WidgetAbilitySettings extends Widget implements Renderer {
    private BindableAttribute<AlgorithmSetting> settings = new BindableAttribute<>(AlgorithmSetting.class);
    public WidgetAbilitySettings(BindableAttribute<AlgorithmSetting> settings) {
        this.settings.exportTo(settings);
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    public static final ResourceLocation abilities = new ResourceLocation("dungeonsguide:textures/features/precalclist/abilities.png");


    private void renderIndex(int x, int y, int index, int width, int height, int type) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(abilities);

        int offsetX = (type % 2) * 256;
        int offsetY = (type / 2) * 256;

        GuiScreen.drawScaledCustomSizeModalRect(
                x, y, (index % 8) * 32 + 0.5f + offsetX, index / 8 * 32 + 0.5f + offsetY,  31, 31, width, height, 512, 512
        );

    }


    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {

        Gui.drawRect(0,0, (int) buildContext.getSize().getWidth(), (int) buildContext.getSize().getHeight(), 0xFFFFFFFF);
        Gui.drawRect(1,1,(int) buildContext.getSize().getWidth() - 1,(int) buildContext.getSize().getHeight() - 1, 0xFF333333);

        AlgorithmSetting algorithmSetting = settings.getValue();
        if (algorithmSetting == null) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int pickX = 5;
        if (algorithmSetting.getPickaxe() != null) {
            renderIndex(pickX, 1, algorithmSetting.getPickaxe().getTool().getToolMaterial().ordinal() * 8, 16, 16, 0);
            String toDraw = algorithmSetting.getPickaxe().getEfficiency() + "";
            fr.drawStringWithShadow(toDraw, pickX + 17 - fr.getStringWidth(toDraw), 10, 0xFF979797);
        } else {
            renderIndex(pickX, 1, 0, 16, 16, 1);
            fr.drawStringWithShadow("X", pickX + 17 - fr.getStringWidth("X"), 10, 0xFFa4232b);
        }

        int shovelX = 24;
        if (algorithmSetting.getShovel() != null) {
            renderIndex(shovelX, 1, algorithmSetting.getShovel().getTool().getToolMaterial().ordinal() * 8 + 1, 16, 16, 0);
            String toDraw = algorithmSetting.getShovel().getEfficiency() + "";
            fr.drawStringWithShadow(toDraw, shovelX + 17 - fr.getStringWidth(toDraw), 10, 0xFF979797);
        } else {
            renderIndex(shovelX, 1, 1, 16, 16, 1);
            fr.drawStringWithShadow("X", shovelX + 17 - fr.getStringWidth("X"), 10, 0xFFa4232b);
        }

        int axeX = 43;
        if (algorithmSetting.getAxe() != null) {
            renderIndex(axeX, 1, algorithmSetting.getAxe().getTool().getToolMaterial().ordinal() * 8 + 2, 16, 16, 0);
            String toDraw = algorithmSetting.getAxe().getEfficiency() + "";
            fr.drawStringWithShadow(toDraw, axeX + 17 - fr.getStringWidth(toDraw), 10, 0xFF979797);
        } else {
            renderIndex(axeX, 1, 2, 16, 16, 1);
            fr.drawStringWithShadow("X", axeX + 17 - fr.getStringWidth("X"), 10, 0xFFa4232b);
        }

        if (algorithmSetting.getAxe() != null || algorithmSetting.getShovel() != null || algorithmSetting.getPickaxe() != null) {
            fr.drawString("D=", axeX + 20, 2, 0xFFFFFFFF);
            fr.drawString(algorithmSetting.getMaxStonk()+"", axeX + 20, 10, 0xFFFFFFFF);
        }

        int hasteX = 76;

        renderIndex(hasteX, 2, 48, 16, 16, algorithmSetting.getHasteLevel() == 0 ? 1 : 0);
        if (algorithmSetting.getHasteLevel() == 0) {
            fr.drawStringWithShadow("X", hasteX + 17 - fr.getStringWidth("X"),10,0xFFa4232b);
        } else {
            fr.drawStringWithShadow(algorithmSetting.getHasteLevel()+"", hasteX + 17 - fr.getStringWidth(algorithmSetting.getHasteLevel()+""),10,0xFFFFFFFF);
        }

        renderIndex(95, 2, 40, 16, 16, algorithmSetting.isRouteEtherwarp() ? 0 : 1);
        if (algorithmSetting.isRouteEtherwarp()) {
            fr.drawString(algorithmSetting.getEtherwarpRadius() + " " + String.format("%.2f", algorithmSetting.getEtherwarpOffset()), 112, 2, 0xFFFFFFFF);
            fr.drawString(String.format("%.4f", algorithmSetting.getEtherwarpLeeway()), 112, 11, 0xFFFFFFFF);
        }


        renderIndex(148, 2, 41, 16, 16, algorithmSetting.isEnderpearl() ? 0 : 1);
        renderIndex(167, 2, 49, 16, 16, algorithmSetting.isTntpearl() ? 0 : 1);
        renderIndex(186, 2, 50, 16, 16, algorithmSetting.isStonkDown() ? 0 : 1);
        renderIndex(205, 2, 56, 16, 16,  algorithmSetting.isStonkTeleport() ? 0 : 1);
        renderIndex(224, 2, 58, 16, 16, algorithmSetting.isStonkEChest() ? 0 : 1);



        // etherwarps and stonks

    }

}

