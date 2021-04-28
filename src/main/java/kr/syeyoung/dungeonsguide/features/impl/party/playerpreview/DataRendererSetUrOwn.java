package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.features.impl.party.api.ClassSpecificData;
import kr.syeyoung.dungeonsguide.features.impl.party.api.PlayerProfile;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import kr.syeyoung.dungeonsguide.utils.XPUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.Arrays;

public class DataRendererSetUrOwn implements DataRenderer {
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§aCustomize at /dg", 0,0,-1);
        fr.drawString("§a-> Party Kicker", 0,fr.FONT_HEIGHT,-1);
        fr.drawString("§a-> View Player Stats", 0,fr.FONT_HEIGHT*2,-1);
        fr.drawString("§a-> Edit", 0,fr.FONT_HEIGHT*3,-1);
        return new Dimension(100, fr.FONT_HEIGHT*4);
    }

    @Override
    public void onHover(PlayerProfile playerProfile, int mouseX, int mouseY) {

    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§aCustomize at /dg", 0,0,-1);
        fr.drawString("§a-> Party Kicker", 0,fr.FONT_HEIGHT,-1);
        fr.drawString("§a-> View Player Stats", 0,fr.FONT_HEIGHT*2,-1);
        fr.drawString("§a-> Edit", 0,fr.FONT_HEIGHT*3,-1);
        return new Dimension(100, fr.FONT_HEIGHT*4);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT*4);
    }
}
