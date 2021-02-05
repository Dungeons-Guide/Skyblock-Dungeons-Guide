package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.lwjgl.opengl.GL11;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FeatureDungeonTombs extends TextHUDFeature {
    public FeatureDungeonTombs() {
        super("Dungeon", "Display #Crypts", "Display how much total crypts have been blown up in a dungeon run", "dungeon.stats.tombs", true, getFontRenderer().getStringWidth("Crypts: 42"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    public int getTombsFound() {
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Crypts: §r§6")) {
                return Integer.parseInt(TextUtils.stripColor(name).substring(9));
            }
        }
        return 0;
    }

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Crypts","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("42","number"));
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "separator", "number"
        });
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Crypts","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(getTombsFound()+"","number"));
        return actualBit;
    }

}
