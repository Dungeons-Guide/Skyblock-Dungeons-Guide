/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.advanced;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.GuiPostRenderListener;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Arrays;

public class FeatureDebuggableMap extends GuiFeature  {
    public FeatureDebuggableMap() {
        super("Advanced", "Display Debug Info included map", "ONLY WORKS WITH SECRET SETTING", "advanced.debug.map", true, 128, 128);
        this.setEnabled(false);
    }


    DynamicTexture dynamicTexture = new DynamicTexture(128, 128);
    ResourceLocation location = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation("dungeons/map/", dynamicTexture);

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
//        if (!skyblockStatus.isOnDungeon()) return;
        if (!FeatureRegistry.DEBUG.isEnabled()) return;
//        DungeonContext context = skyblockStatus.getContext();
//        if (context == null) return;

        GlStateManager.pushMatrix();
        double factor = getFeatureRect().getRectangle().getWidth() / 128;
        GlStateManager.scale(factor, factor, 1);
        int[] textureData = dynamicTexture.getTextureData();
        MapUtils.getImage().getRGB(0, 0, 128, 128, textureData, 0, 128);
        dynamicTexture.updateDynamicTexture();
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        GlStateManager.enableAlpha();
        GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 128, 128, 128, 128);
        GlStateManager.popMatrix();


        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) return;
        Rectangle featureRect = this.getFeatureRect().getRectangleNoScale();

        int i = (int) ((int) (Mouse.getEventX() - featureRect.getX()) / factor);
        int j = (int) ((int) (Minecraft.getMinecraft().displayHeight - Mouse.getEventY() - featureRect.getY())/ factor);
        if (i >= 0 && j>= 0 && i <= 128 && j <= 128 && MapUtils.getColors() != null) {
            GuiUtils.drawHoveringText(Arrays.asList(i+","+j,"Color: "+MapUtils.getColors()[j * 128 + i]),(int)(Mouse.getEventX() - featureRect.getX()), (int) (Minecraft.getMinecraft().displayHeight - Mouse.getEventY() - featureRect.getY()), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, -1, Minecraft.getMinecraft().fontRendererObj);
        }
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        Rectangle featureRect = getFeatureRect().getRectangle();
        GL11.glLineWidth(2);
        RenderUtils.drawUnfilledBox(0,0,featureRect.width, featureRect.height, 0xff000000, false);
    }
}
