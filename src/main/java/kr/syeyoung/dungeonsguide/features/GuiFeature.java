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

package kr.syeyoung.dungeonsguide.features;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfigV2;
import kr.syeyoung.dungeonsguide.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.config.types.GUIRectangle;
import kr.syeyoung.dungeonsguide.config.types.TypeConverterRegistry;
import kr.syeyoung.dungeonsguide.features.listener.ScreenRenderListener;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class GuiFeature extends AbstractFeature implements ScreenRenderListener {
    @Setter
    private GUIRectangle featureRect;
    @Setter(value = AccessLevel.PROTECTED)
    private boolean keepRatio;
    @Setter(value = AccessLevel.PROTECTED)
    private double defaultWidth;
    @Setter(value = AccessLevel.PROTECTED)
    private double defaultHeight;
    private final double defaultRatio;

    protected GuiFeature(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super(category, name, description, key);
        this.keepRatio = keepRatio;
        this.defaultWidth = width;
        this.defaultHeight = height;
        this.defaultRatio = defaultWidth / defaultHeight;
        this.featureRect = new GUIRectangle(0, 0, width, height);
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (!isEnabled()) return;

        GlStateManager.pushMatrix();
        Rectangle featureRect = this.featureRect.getRectangleNoScale();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.scale(1.0/scaledResolution.getScaleFactor(), 1.0/scaledResolution.getScaleFactor(), 1);
        clip(featureRect.x, featureRect.y, featureRect.width, featureRect.height);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GlStateManager.translate(featureRect.x, featureRect.y, 0);
        drawHUD(partialTicks);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();


        GlStateManager.enableBlend();
        GlStateManager.color(1,1,1,1);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }


    public abstract void drawHUD(float partialTicks);

    public void drawDemo(float partialTicks) {
        drawHUD(partialTicks);
    }

    private void clip(int x, int y, int width, int height) {
//        int scale = resolution.getScaleFactor();
        int scale = 1;
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }

    public static FontRenderer getFontRenderer() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        return fr;
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        this.featureRect = TypeConverterRegistry.getTypeConverter("guirect",GUIRectangle.class).deserialize(jsonObject.get("$bounds"));
    }

    @Override
    public JsonObject saveConfig() {
        JsonObject object = super.saveConfig();
        object.add("$bounds", TypeConverterRegistry.getTypeConverter("guirect", GUIRectangle.class).serialize(featureRect));
        return object;
    }

    public List<MPanel> getTooltipForEditor(GuiGuiLocationConfig guiGuiLocationConfig) {
        ArrayList<MPanel> mPanels = new ArrayList<>();
        mPanels.add(new MLabel(){
            {
                setText(getName());
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(Minecraft.getMinecraft().fontRendererObj.getStringWidth(getName()), 20);
            }
        });
        mPanels.add(new MButton() {
            {
                setText("Edit");
                setOnActionPerformed(() -> {
                    GuiScreen guiScreen = guiGuiLocationConfig.getBefore();
                    if (guiScreen == null) {
                        guiScreen = new GuiConfigV2();
                    }
                    Minecraft.getMinecraft().displayGuiScreen(guiScreen);
                    if (guiScreen instanceof GuiConfigV2) {
                        ((GuiConfigV2) guiScreen).getRootConfigPanel().setCurrentPageAndPushHistory(getEditRoute(((GuiConfigV2) guiScreen).getRootConfigPanel()));
                    }
                });
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100,20);
            }
        });
        mPanels.add(new MPassiveLabelAndElement("Enabled", new MToggleButton() {{
            setEnabled(GuiFeature.this.isEnabled());
            setOnToggle(() ->{
                GuiFeature.this.setEnabled(isEnabled());
            }); }
        }));
        return mPanels;
    }
}
