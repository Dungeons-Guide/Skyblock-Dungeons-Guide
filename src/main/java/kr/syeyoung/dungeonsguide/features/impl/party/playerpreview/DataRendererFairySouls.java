package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.features.impl.party.api.DungeonSpecificData;
import kr.syeyoung.dungeonsguide.features.impl.party.api.DungeonStat;
import kr.syeyoung.dungeonsguide.features.impl.party.api.DungeonType;
import kr.syeyoung.dungeonsguide.features.impl.party.api.PlayerProfile;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import kr.syeyoung.dungeonsguide.utils.XPUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.Arrays;

public class DataRendererFairySouls implements DataRenderer {
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§eFairy Souls §f"+playerProfile.getFairySouls(), 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§eFairy Souls §f300", 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT);
    }

    @Override
    public void onHover(PlayerProfile playerProfile, int mouseX, int mouseY) {
    }
}
