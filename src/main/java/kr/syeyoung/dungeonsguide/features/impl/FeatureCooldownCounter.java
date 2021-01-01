package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FeatureCooldownCounter extends GuiFeature implements TickListener {
    public FeatureCooldownCounter() {
        super("QoL", "Dungeon Cooldown Counter", "Counts 10 seconds after leaving dungeon", "qol.cooldown", true, getFontRenderer().getStringWidth("Cooldown: 10s "), getFontRenderer().FONT_HEIGHT);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.white, "color"));
    }

    @Override
    public void drawHUD(float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);
        fr.drawString("Cooldown: 1s", 0,0,this.<Color>getParameter("color").getValue().getRGB());
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);
        fr.drawString("Cooldown: 10s", 0,0,this.<Color>getParameter("color").getValue().getRGB());
    }

    @Override
    public void onTick() {

    }
}
