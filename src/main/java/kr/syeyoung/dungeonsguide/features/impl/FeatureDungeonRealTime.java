package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.SimpleDateFormat;

public class FeatureDungeonRealTime extends GuiFeature implements TickListener {
    public FeatureDungeonRealTime() {
        super("Dungeon", "Display Real Time-Dungeon Time", "Display how much real time has passed since dungeon run started", "dungeon.stats.realtime", true, getFontRenderer().getStringWidth("Time(Real): 59m 59s"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.orange, "color"));
    }

    private long started = -1;
    @Override
    public void drawHUD(float partialTicks) {
        if (started == -1) return;
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);
        fr.drawString("Time(Real): "+TextUtils.formatTime(getTimeElapsed()), 0,0, this.<Color>getParameter("color").getValue().getRGB());
    }

    public long getTimeElapsed() {
        return System.currentTimeMillis() - started;
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);
        fr.drawString("Time(Real): -42h", 0,0, this.<Color>getParameter("color").getValue().getRGB());
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    private boolean wasInDungeon = false;
    @Override
    public void onTick() {
        if (wasInDungeon && !skyblockStatus.isOnDungeon()) {
            if (skyblockStatus.isOnSkyblock()) started = -1;
        } else if (!wasInDungeon && skyblockStatus.isOnDungeon()) {
            started = System.currentTimeMillis();
        }
        wasInDungeon = skyblockStatus.isOnDungeon();
    }
}
