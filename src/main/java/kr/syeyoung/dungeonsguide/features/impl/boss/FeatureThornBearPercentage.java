package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.HealthData;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureThornBearPercentage extends TextHUDFeature {
    public FeatureThornBearPercentage() {
        super("Bossfight", "Display Spirit Bear Summon Percentage", "Displays spirit bear summon percentage in hud", "bossfight.spiritbear", true, getFontRenderer().getStringWidth("Spirit Bear: 100%"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(true);
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Spirit Bear","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("50","number"));
        dummyText.add(new StyledText("%","unit"));
    }
    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorThorn;
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "separator", "number", "unit"
        });
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        int percentage = (int) (((BossfightProcessorThorn) skyblockStatus.getContext().getBossfightProcessor()).calculatePercentage() * 100);
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Spirit Bear","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(percentage+"","number"));
        actualBit.add(new StyledText("%","unit"));
        return actualBit;
    }

}
