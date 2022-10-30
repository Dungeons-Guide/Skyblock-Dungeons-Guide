package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureDebugTrap extends TextHUDFeature {
    public FeatureDebugTrap() {
        super("Debug", "Display the current amount of bat entities", "", "advanced.bat", false, getFontRenderer().getStringWidth("Bats: 9999"), getFontRenderer().FONT_HEIGHT);
        getStyles().add(new TextStyle("base", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("batsamm", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }
    
    

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private static final List<StyledText> dummyText=  new ArrayList<>();
    static {
        dummyText.add(new StyledText("Bats: ","base"));
        dummyText.add(new StyledText("9999","batsamm"));
    }


    static List<Long> avgStorage = new ArrayList<>();

    public static void updateVal(long timeItTookForThePacketToProcess){
        System.out.println(timeItTookForThePacketToProcess);
        if(avgStorage.size() > 5){
            avgStorage.remove( 0);
        }

        avgStorage.add(timeItTookForThePacketToProcess);
    }


    static long getAvgTimeItTookToPAcket(){
        return (long) avgStorage.stream().mapToLong(val -> val).average().orElse(0.0);
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getMapProcessor() != null;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("batsamm", "base");
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<StyledText> getText() {

        List<Entity> bats = Minecraft.getMinecraft().theWorld.getEntities(EntityBat.class, e -> true);


        List<StyledText> actualBit = new ArrayList<>();
        actualBit.add(new StyledText("Bats: ","base"));


        actualBit.add(new StyledText(String.valueOf(getAvgTimeItTookToPAcket()),"batsamm"));

        return actualBit;
    }
}
