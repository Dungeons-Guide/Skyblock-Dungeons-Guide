package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorSadan;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.entity.boss.BossStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureTerracotaTimer extends TextHUDFeature {
    public FeatureTerracotaTimer() {
        super("Bossfight", "Display Terracotta phase timer", "Displays Terracotta phase timer", "bossfight.terracota", true, getFontRenderer().getStringWidth("Terracottas: 1m 99s"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(true);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("time", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Terracottas","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("1m 99s","time"));
    }
    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorSadan &&
                "fight-1".equalsIgnoreCase(skyblockStatus.getContext().getBossfightProcessor().getCurrentPhase());
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "separator", "number", "unit"
        });
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Terracottas","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(TextUtils.formatTime((long) (BossStatus.healthScale * 1000 * 60 * 1.5)),"time"));
        return actualBit;
    }

}
