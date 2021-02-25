package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorThorn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureCurrentPhase extends TextHUDFeature {
    public FeatureCurrentPhase() {
        super("Bossfight", "Display Current Phase", "Displays the current phase of bossfight", "bossfight.phasedisplay", false, getFontRenderer().getStringWidth("Current Phase: fight-2-idk-howlng"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(true);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("phase", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Current Phase","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("fight-2","phase"));
    }
    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() != null;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "separator", "phase"
        });
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<StyledText> getText() {
        String currentPhsae =skyblockStatus.getContext().getBossfightProcessor().getCurrentPhase();
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Current Phase","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(currentPhsae,"phase"));
        return actualBit;
    }

}
