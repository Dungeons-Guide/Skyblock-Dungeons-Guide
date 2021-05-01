package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.features.impl.party.api.PlayerProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.stream.Collectors;

public class DataRendererSecrets implements DataRenderer {
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double theint = playerProfile.getTotalSecrets()/ (double)playerProfile.getDungeonStats().values().stream().flatMap(s -> s.getData().getPlays().values().stream())
                .map(fs -> fs.getData().getWatcherKills()).reduce(0, Integer::sum);
        fr.drawString("§eSecrets §b"+playerProfile.getTotalSecrets()+" §7("+
                String.format("%.2f", theint)+"/run)", 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§eSecrets §b99999 §7(X/run)", 0,0,-1);
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
