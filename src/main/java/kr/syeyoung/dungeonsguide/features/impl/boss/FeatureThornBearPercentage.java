package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.HealthData;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.List;

public class FeatureThornBearPercentage extends GuiFeature {
    public FeatureThornBearPercentage() {
        super("Bossfight", "Display Spirit Bear Summon Percentage", "Displays spirit bear summon percentage in hud", "bossfight.spiritbear", true, getFontRenderer().getStringWidth("Spirit Bear: 100%"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(true);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.orange, "color"));
     }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
        if (!skyblockStatus.isOnDungeon()) return;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return;
        if (!(context.getBossfightProcessor() instanceof BossfightProcessorThorn)) return;
        int percentage = (int) (((BossfightProcessorThorn) context.getBossfightProcessor()).calculatePercentage() * 100);
        FontRenderer fr = getFontRenderer();
        fr.drawString("Spirit Bear: "+percentage+"%", 0,0, this.<Color>getParameter("color").getValue().getRGB());
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        fr.drawString("Spirit Bear: 50%", 0,0, this.<Color>getParameter("color").getValue().getRGB());
    }
}
