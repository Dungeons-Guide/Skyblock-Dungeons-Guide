package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.features.GuiFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

public class FeatureCooldownCounter extends GuiFeature {
    public FeatureCooldownCounter() {
        super("QoL", "Dungeon Cooldown Counter", "Counts 10 seconds after leaving dungeon", "qol.cooldown", true, 100, 50);
    }

    @Override
    public void drawHUD(float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);
        fr.drawString("Cooldown: 1s", 0,0,0xFFFFFFFF);
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);
        fr.drawString("Cooldown: 1s", 0,0,0xFFFFFFFF);
    }
}
