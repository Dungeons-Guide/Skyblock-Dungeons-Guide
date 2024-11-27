package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.AlgorithmSettings;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

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

    public static final ResourceLocation abilities = new ResourceLocation("dungeonsguide:textures/features/precalclist/abilities.png");


    private void renderIndex(int x, int y, int index, int width, int height) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(abilities);
        GuiScreen.drawScaledCustomSizeModalRect(
                x, y, (index % 8) * 32 + 0.5f, index / 8 * 32 + 0.5f,  31, 31, width, height, 256, 256
        );

    }


    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {

        Gui.drawRect(0,0, (int) buildContext.getSize().getWidth(), (int) buildContext.getSize().getHeight(), 0xFFFFFFFF);
        Gui.drawRect(1,1,(int) buildContext.getSize().getWidth() - 1,(int) buildContext.getSize().getHeight() - 1, 0xFF333333);

        AlgorithmSettings algorithmSettings = settings.getValue();
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int pickX = 5;
        if (algorithmSettings.getPickaxe() != null) {
            renderIndex(pickX, 1, algorithmSettings.getPickaxe().getTool().getToolMaterial().ordinal() * 8, 16, 16);
            String toDraw = algorithmSettings.getPickaxe().getEfficiency() + "";
            fr.drawStringWithShadow(toDraw, pickX + 17 - fr.getStringWidth(toDraw), 10, 0xFF979797);
        } else {
            renderIndex(pickX, 1, 4, 16, 16);
            fr.drawStringWithShadow("X", pickX + 17 - fr.getStringWidth("X"), 10, 0xFFa4232b);
        }

        int shovelX = 24;
        if (algorithmSettings.getShovel() != null) {
            renderIndex(shovelX, 1, algorithmSettings.getShovel().getTool().getToolMaterial().ordinal() * 8 + 1, 16, 16);
            String toDraw = algorithmSettings.getShovel().getEfficiency() + "";
            fr.drawStringWithShadow(toDraw, shovelX + 17 - fr.getStringWidth(toDraw), 10, 0xFF979797);
        } else {
            renderIndex(shovelX, 1, 5, 16, 16);
            fr.drawStringWithShadow("X", shovelX + 17 - fr.getStringWidth("X"), 10, 0xFFa4232b);
        }

        int axeX = 43;
        if (algorithmSettings.getAxe() != null) {
            renderIndex(axeX, 1, algorithmSettings.getAxe().getTool().getToolMaterial().ordinal() * 8 + 2, 16, 16);
            String toDraw = algorithmSettings.getAxe().getEfficiency() + "";
            fr.drawStringWithShadow(toDraw, axeX + 17 - fr.getStringWidth(toDraw), 10, 0xFF979797);
        } else {
            renderIndex(axeX, 1, 6, 16, 16);
            fr.drawStringWithShadow("X", axeX + 17 - fr.getStringWidth("X"), 10, 0xFFa4232b);
        }

        if (algorithmSettings.getAxe() != null || algorithmSettings.getShovel() != null || algorithmSettings.getPickaxe() != null) {
            fr.drawString("D=", axeX + 20, 2, 0xFFFFFFFF);
            fr.drawString(algorithmSettings.getMaxStonk()+"", axeX + 20, 10, 0xFFFFFFFF);
        }

        int hasteX = 76;

        renderIndex(hasteX, 2, 48, 16, 16);
        if (algorithmSettings.getHasteLevel() == 0) {
            fr.drawStringWithShadow("X", hasteX + 17 - fr.getStringWidth(""),10,0xFFa4232b);
        } else {
            fr.drawStringWithShadow(algorithmSettings.getHasteLevel()+"", hasteX + 17 - fr.getStringWidth(algorithmSettings.getHasteLevel()+""),10,0xFFFFFFFF);
        }

        renderIndex(95, 2, algorithmSettings.isRouteEtherwarp() ? 40 : 44, 16, 16);
        if (algorithmSettings.isRouteEtherwarp()) {
            fr.drawString(algorithmSettings.getEtherwarpRadius() + " " + String.format("%.2f", algorithmSettings.getEtherwarpOffset()), 112, 2, 0xFFFFFFFF);
            fr.drawString(String.format("%.4f", algorithmSettings.getEtherwarpLeeway()), 112, 11, 0xFFFFFFFF);
        }


        renderIndex(148, 2, algorithmSettings.isEnderpearl() ? 41 : 45, 16, 16);
        renderIndex(167, 2, algorithmSettings.isTntpearl() ? 49 : 53, 16, 16);
        renderIndex(186, 2, algorithmSettings.isStonkDown() ? 50 : 54, 16, 16);
        renderIndex(205, 2, algorithmSettings.isStonkTeleport() ? 56 : 60, 16, 16);
        renderIndex(224, 2, algorithmSettings.isStonkEChest() ? 58 : 62, 16, 16);



        // etherwarps and stonks

    }

}

