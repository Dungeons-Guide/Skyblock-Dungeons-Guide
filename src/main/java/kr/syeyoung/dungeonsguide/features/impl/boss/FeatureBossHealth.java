package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.HealthData;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.awt.*;
import java.awt.font.TextHitInfo;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.List;
import java.util.regex.Pattern;

public class FeatureBossHealth extends GuiFeature {
    public FeatureBossHealth() {
        super("Bossfight", "Display Boss Health(s)", "Show the health of boss and minibosses in bossfight (Guardians, Priests..)", "bossfight.health", false, getFontRenderer().getStringWidth("The Professor: 4242m"), getFontRenderer().FONT_HEIGHT * 5);
        this.setEnabled(true);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.orange, "color"));
        parameters.put("totalHealth", new FeatureParameter<Boolean>("totalHealth", "show total health", "Show total health along with current health", false, "boolean"));
        parameters.put("formatHealth", new FeatureParameter<Boolean>("formatHealth", "format health", "1234568 -> 1m", true, "boolean"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
        if (!skyblockStatus.isOnDungeon()) return;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return;
        if (context.getBossfightProcessor() == null) return;
        List<HealthData> healths = context.getBossfightProcessor().getHealths();
        int i = 0;
        FontRenderer fr = getFontRenderer();
        boolean format = this.<Boolean>getParameter("formatHealth").getValue();
        boolean total = this.<Boolean>getParameter("totalHealth").getValue();
        for (HealthData heal : healths) {
            fr.drawString(heal.getName() + ": " + (format ? TextUtils.format(heal.getHealth()) : heal.getHealth()) + (total ? "/"+(format ? TextUtils.format(heal.getMaxHealth()) : heal.getMaxHealth()) : ""), 0, i, this.<Color>getParameter("color").getValue().getRGB());
            i += 8;
        }
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        fr.drawString("The Professor: 3.3m", 0,0, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("Chaos Guardian: 500k", 0,8, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("Healing Guardian: 1m", 0,16, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("Laser Guardian: 5m", 0,24, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("Giant: 10m", 0,32, this.<Color>getParameter("color").getValue().getRGB());
        }
}
