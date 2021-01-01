package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
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

    private long leftDungeonTime = 0L;
    private boolean wasInDungeon = false;
    @Override
    public void drawHUD(float partialTicks) {
        if (System.currentTimeMillis() - leftDungeonTime > 10000) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);
        fr.drawString("Cooldown: "+(10 - (System.currentTimeMillis() - leftDungeonTime) / 1000)+"s", 0,0,this.<Color>getParameter("color").getValue().getRGB());
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);
        fr.drawString("Cooldown: 10s", 0,0,this.<Color>getParameter("color").getValue().getRGB());
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void onTick() {
        if (wasInDungeon && !skyblockStatus.isOnDungeon()) {
            if (skyblockStatus.isOnSkyblock())
                leftDungeonTime = System.currentTimeMillis();
            else return;
            System.out.println("think he left");
        }
        wasInDungeon = skyblockStatus.isOnDungeon();
    }
}
