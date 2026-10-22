package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetToolEdit;

import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;

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

    public static final Identifier abilities = Identifier.parse("dungeonsguide:textures/features/precalclist/abilities.png");


    private void renderIndex(RenderingContext context, int x, int y, int index, int width, int height, int type) {

        int offsetX = (type % 2) * 256;
        int offsetY = (type / 2) * 256;

        context.drawScaledCustomSizeModalRect(abilities,
                x, y, (index % 8) * 32 + 0.5f + offsetX, index / 8 * 32 + 0.5f + offsetY,  31, 31, width, height, 512, 512
        );

    }


    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {

        context.drawRect(0,0, (int) buildContext.getSize().getWidth(), (int) buildContext.getSize().getHeight(), 0xFFFFFFFF);
        context.drawRect(1,1,(int) buildContext.getSize().getWidth() - 1,(int) buildContext.getSize().getHeight() - 1, 0xFF333333);

        AlgorithmSetting algorithmSetting = settings.getValue();
        if (algorithmSetting == null) return;
        Font fr = Minecraft.getInstance().font;

        int pickX = 5;
        if (algorithmSetting.getPickaxe() != null) {
            renderIndex(context, pickX, 1, WidgetToolEdit.ToolMaterialType.of(algorithmSetting.getPickaxe().getTool()).index, 16, 16, 0);
            String toDraw = algorithmSetting.getPickaxe().getEfficiency() + "";
            context.drawString(toDraw, pickX + 17 - fr.width(toDraw), 10, 0xFF979797, true);
        } else {
            renderIndex(context, pickX, 1, 0, 16, 16, 1);
            context.drawString("X", pickX + 17 - fr.width("X"), 10, 0xFFa4232b, true);
        }

        int shovelX = 24;
        if (algorithmSetting.getShovel() != null) {
            renderIndex(context, shovelX, 1, WidgetToolEdit.ToolMaterialType.of(algorithmSetting.getShovel().getTool()).index + 1, 16, 16, 0);
            String toDraw = algorithmSetting.getShovel().getEfficiency() + "";
            context.drawString(toDraw, shovelX + 17 - fr.width(toDraw), 10, 0xFF979797, true);
        } else {
            renderIndex(context, shovelX, 1, 1, 16, 16, 1);
            context.drawString("X", shovelX + 17 - fr.width("X"), 10, 0xFFa4232b, true);
        }

        int axeX = 43;
        if (algorithmSetting.getAxe() != null) {
            renderIndex(context, axeX, 1, WidgetToolEdit.ToolMaterialType.of(algorithmSetting.getAxe().getTool()).index + 2, 16, 16, 0);
            String toDraw = algorithmSetting.getAxe().getEfficiency() + "";
            context.drawString(toDraw, axeX + 17 - fr.width(toDraw), 10, 0xFF979797, true);
        } else {
            renderIndex(context, axeX, 1, 2, 16, 16, 1);
            context.drawString("X", axeX + 17 - fr.width("X"), 10, 0xFFa4232b, true);
        }

        if (algorithmSetting.getAxe() != null || algorithmSetting.getShovel() != null || algorithmSetting.getPickaxe() != null) {
            context.drawString("D=", axeX + 20, 2, 0xFFFFFFFF, false);
            context.drawString(algorithmSetting.getMaxStonk()+"", axeX + 20, 10, 0xFFFFFFFF, false);
        }

        int hasteX = 76;

        renderIndex(context, hasteX, 2, 48, 16, 16, algorithmSetting.getHasteLevel() == 0 ? 1 : 0);
        if (algorithmSetting.getHasteLevel() == 0) {
            context.drawString("X", hasteX + 17 - fr.width("X"),10,0xFFa4232b, true);
        } else {
            context.drawString(algorithmSetting.getHasteLevel()+"", hasteX + 17 - fr.width(algorithmSetting.getHasteLevel()+""),10,0xFFFFFFFF, true);
        }

        renderIndex(context, 95, 2, 40, 16, 16, algorithmSetting.isRouteEtherwarp() ? 0 : 1);
        if (algorithmSetting.isRouteEtherwarp()) {
            context.drawString(algorithmSetting.getEtherwarpRadius() + " " + String.format("%.2f", algorithmSetting.getEtherwarpOffset()), 112, 2, 0xFFFFFFFF, false);
            context.drawString(String.format("%.4f", algorithmSetting.getEtherwarpLeeway()), 112, 11, 0xFFFFFFFF, false);
        }


        renderIndex(context, 148, 2, 41, 16, 16, algorithmSetting.isEnderpearl() ? 0 : 1);
        renderIndex(context, 167, 2, 49, 16, 16, algorithmSetting.isTntpearl() ? 0 : 1);
        renderIndex(context, 186, 2, 50, 16, 16, algorithmSetting.isStonkDown() ? 0 : 1);
        renderIndex(context, 205, 2, 56, 16, 16,  algorithmSetting.isStonkTeleport() ? 0 : 1);
        renderIndex(context, 224, 2, 58, 16, 16, algorithmSetting.isStonkEChest() ? 0 : 1);



        // etherwarps and stonks

    }

}

