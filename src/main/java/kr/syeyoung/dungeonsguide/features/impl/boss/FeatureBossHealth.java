package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.HealthData;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.awt.*;
import java.awt.font.TextHitInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.List;
import java.util.regex.Pattern;

public class FeatureBossHealth extends TextHUDFeature {
    public FeatureBossHealth() {
        super("Bossfight", "Display Boss Health(s)", "Show the health of boss and minibosses in bossfight (Guardians, Priests..)", "bossfight.health", false, getFontRenderer().getStringWidth("The Professor: 4242m"), getFontRenderer().FONT_HEIGHT * 5);
        this.setEnabled(true);
        parameters.put("totalHealth", new FeatureParameter<Boolean>("totalHealth", "show total health", "Show total health along with current health", false, "boolean"));
        parameters.put("formatHealth", new FeatureParameter<Boolean>("formatHealth", "format health", "1234568 -> 1m", true, "boolean"));
        parameters.put("ignoreInattackable", new FeatureParameter<Boolean>("ignoreInattackable", "Don't show health of in-attackable enemy", "For example, do not show guardians health when they're not attackable", false, "boolean"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();


    @Override
    public boolean doesScaleWithHeight() {
        return false;
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() != null;
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "separator", "health", "separator2", "maxHealth"
        });
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        addLine(new HealthData("The Professor", 3300000, 5000000, false), actualBit);
        addLine(new HealthData("Chaos Guardian", 500000, 2000000, true), actualBit);
        addLine(new HealthData("Healing Guardian", 1000000, 3000000, true), actualBit);
        addLine(new HealthData("Laser Guardian", 5000000, 5000000, true), actualBit);
        addLine(new HealthData("Giant", 10000000, 20000000, false), actualBit);
        return actualBit;
    }

    public void addLine(HealthData data, List<StyledText> actualBit) {
        boolean format = this.<Boolean>getParameter("formatHealth").getValue();
        boolean total = this.<Boolean>getParameter("totalHealth").getValue();
        boolean ignore = this.<Boolean>getParameter("ignoreInattackable").getValue();
        if (ignore && !data.isAttackable()) return;

        actualBit.add(new StyledText(data.getName(),"title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText( (format ? TextUtils.format(data.getHealth()) : data.getHealth()) + (total ? "" : "\n"),"health"));
        if (total) {
            actualBit.add(new StyledText("/", "separator2"));
            actualBit.add(new StyledText( (format ? TextUtils.format(data.getMaxHealth()) : data.getMaxHealth()) +"\n","maxHealth"));
        }
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        List<HealthData> healths = skyblockStatus.getContext().getBossfightProcessor().getHealths();
        for (HealthData heal : healths) {
            addLine(heal, actualBit);
        }
        return actualBit;
    }
}
